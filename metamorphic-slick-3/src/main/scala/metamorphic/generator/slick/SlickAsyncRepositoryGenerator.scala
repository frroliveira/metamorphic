package metamorphic.generator.slick

import metamorphic.MetamorphicException
import metamorphic.dsl.model._
import metamorphic.dsl.generator._
import metamorphic.dsl.util.StringImplicits._
import metamorphic.dsl.util.Instantiator

import scala.reflect.macros.blackbox.Context

class SlickAsyncRepositoryGenerator extends RepositoryGenerator {

  val driverGenerator = Instantiator.instance[SlickAsyncDriverGenerator](SlickSettings.driverGenerator)

  def name: String = "slick"
  
  def isAsync: Boolean = true

  def generate(model: Model)(implicit c: Context): List[c.Tree] = {
    import c.universe._

    driverGenerator match {
      case None => c.abort(c.enclosingPosition, "Slick driver not found.")
      case Some(generator) => {
        try {
          generator.validate
        } catch {
          case e: MetamorphicException => c.abort(c.enclosingPosition, e.getMessage)
        }
      }
    }

    val imports: List[Tree] = model.entities.map(entity => q"import ${entity.obj.toTerm}._")
    val repositories: List[Tree] = model.entities.flatMap(entity => repository(model, entity))
    
    q"""
      ..$imports
      import slick.driver.JdbcDriver
      import slick.util.AsyncExecutor
          
      trait Profile {
        import com.github.tototoshi.slick.GenericJodaSupport
        
        val driver: JdbcDriver
        object jodaSupport extends GenericJodaSupport(driver)
      }
       
      ..$repositories
    """.children
  }
  
  private def repository(model: Model, entity: Entity)(implicit c: Context): List[c.Tree] = {
    import c.universe._
    
    val manyToMany = entity.manyToManyToImplement

    def repositoryObject: Tree = {
    
      val daos: List[Tree] = new SlickAsyncDaoGenerator(entity).generate
      val rows: List[Tree] = new SlickRowGenerator(entity).generate        
      val components: List[Tree] = new SlickAsyncComponentGenerator(entity).generate
      
      q"""
        object ${entity.obj.toTerm} {
          ..$rows
          ..$components
          ..$daos
        }
      """
    }
    
    def repositoryClass: Tree = {
      
      q"""
        class ${entity.repository.toType} {
          import slick.jdbc.JdbcBackend.Database
          import slick.dbio.DBIO
      
          ..$declareDaos
          val db = ${driverGenerator.get.database}
          
          def getAll: Future[List[${entity.toType}]] = {
            db.run($actionForGetAll)
          }
          
          def get(id: Int): Future[Option[${entity.toType}]] = {
            db.run($actionForGet)
          }
          
          def create(instance: ${entity.toType}): Future[${entity.toType}] = {
            db.run($actionForCreate)
          }
          
          def replace(instance: ${entity.toType}): Future[Option[${entity.toType}]] = {
            db.run($actionForReplace)
          }
          
          def delete(id: Int): Future[Boolean] = {
            db.run(${entity.dao.obj.toTerm}.delete(id))
              .map(count => count == 1)
          }
          
          ..$createIfNotExists
        }
      """
    }
    
    def declareDaos: List[Tree] = {
    
      q"""
        val ${entity.dao.obj.toTerm} =
          new ${entity.dao.toType}(${driverGenerator.get.driver})
      """ ::
      manyToMany.map(property =>
      q"""
        val ${entity.merge(property).dao.obj.toTerm} = 
          new ${entity.merge(property).dao.toType}(${driverGenerator.get.driver})
      """)
    }

    def actionForGetAll: Tree = {

      if (manyToMany.isEmpty) {
        q"""
          for (
            sequence <- ${entity.dao.obj.toTerm}.getAll
          ) yield {
            sequence.map(row => $rowToEntity).toList
          }
        """
      } else {
        q"""
          (
            for {
              sequence <- ${entity.dao.obj.toTerm}.getAll
              result <- DBIO.sequence(
                sequence
                  .map(row => for (..${getIdsActions(false)}) yield $rowToEntity)
                  .toList
              )
            } yield result
          ).withPinnedSession
        """
      }
    }
    
    def actionForGet: Tree = {
        
      if (manyToMany.isEmpty) {
        q"""
          for (
            row <- ${entity.dao.obj.toTerm}.get(id)
          ) yield {
            row.map(row => $rowToEntity)
          }
        """
      } else {
        q"""
          (
            for {
              row <- ${entity.dao.obj.toTerm}.get(id)
              result <- row match {
                case None => DBIO.successful(None)
                case Some(row) => for (..${getIdsActions(true)}) yield Some($rowToEntity)
              }
            } yield result
          ).withPinnedSession
        """
      }
    }
    
    def getIdsActions(option: Boolean): List[Tree] = {
    
      val idTerm = if (option) q"id" else q"row.id.get"

      entity.manyToManyToImplement.map(property =>
        fq"""
          ${property.toTerm} <-
            ${entity.merge(property).dao.obj.toTerm}
              .${property.end2.className.getIds.toTerm}($idTerm)
        """
      )
    }
    
    def actionForCreate: Tree = {

      val actions = fq"id <- ${entity.dao.obj.toTerm}.create($entityToRow)" :: createActions(false)
      q"""
        (
          for (
            ..$actions              
          ) yield {
            instance.copy(id = Some(id))
          }
        ).withPinnedSession
      """
    }

    def createActions(onReplace: Boolean): List[Tree] = {

      val idTerm = if (onReplace) q"instance.id.get" else q"id"
      
      manyToMany.map(property =>
        fq"""
          _ <- ${entity.merge(property).dao.obj.toTerm}.create(
            instance.${property.name.toTerm}
              .map(propertyId => ${entity.merge(property).row.toTerm}($idTerm, propertyId))
          )
        """
      )
    }
        
    def actionForReplace: Tree = {
    
      val actions = fq"count <- ${entity.dao.obj.toTerm}.replace($entityToRow)" ::
        (deleteActions ++ createActions(true))
      
      q"""
        (
          for (
            ..$actions              
          ) yield {
            if (count == 1) {
              Some(instance)
            } else {
              None
            }
          }
        ).withPinnedSession
      """
    }
    
    def deleteActions: List[Tree] = {

      manyToMany.map(property =>
        fq"""
          _ <- ${entity.merge(property).dao.obj.toTerm}
            .${entity.deleteById.toTerm}(instance.id.get)
        """
      )
    }
    
    def entityToRow: Tree = {
    
      val rows = q"instance.id" :: entity.properties
        .filter(property => property.isImplementedAsRow)
        .map(property => q"instance.${property.toTerm}")
      
      q"${entity.row.toTerm}(..$rows)"
    }
    
    def rowToEntity: Tree = {
    
      val values = q"row.id" :: entity.properties
        .filter(property => property.isImplementedAsRow || property.isImplementedAsManyToMany)
        .map(property => {
          if (property.isImplementedAsRow) q"row.${property.toTerm}"
          else q"${property.toTerm}.toList"
        });

      q"${entity.name.toTerm}(..$values)"
    }
    
    def createIfNotExists: List[Tree] = {
    
      q"""
        db.run(${entity.dao.obj.toTerm}.table)
          .onSuccess {
            case None => db.run(${entity.dao.obj.toTerm}.createSchema)
          }
      """ ::
      manyToMany.map(property =>
      q"""
        db.run(${entity.merge(property).dao.obj.toTerm}.table)
          .onSuccess {
            case None => db.run(${entity.merge(property).dao.obj.toTerm}.createSchema)
          }
      """)
    }
    
    repositoryObject :: repositoryClass :: Nil
  }
}
