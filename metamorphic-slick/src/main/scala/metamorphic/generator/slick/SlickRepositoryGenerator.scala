package metamorphic.generator.slick

import metamorphic.MetamorphicException
import metamorphic.dsl.model._
import metamorphic.dsl.generator._
import metamorphic.dsl.util.StringImplicits._
import metamorphic.dsl.util.Instantiator

import scala.reflect.macros.blackbox.Context

class SlickRepositoryGenerator extends RepositoryGenerator {

  val driverGenerator = Instantiator.instance[SlickDriverGenerator](SlickSettings.driverGenerator)

  def name: String = "slick"
  
  def isAsync: Boolean = false

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
      import scala.slick.driver.JdbcDriver
      
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
       
    def repositoryObject: Tree = {
      val daos: List[Tree] = new SlickDaoGenerator(entity).generate
      val rows: List[Tree] = new SlickRowGenerator(entity).generate        
      val components: List[Tree] = new SlickComponentGenerator(entity).generate
      
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
          import scala.slick.jdbc.JdbcBackend.{Database, Session}

          ..$declareDaos
          val db = ${driverGenerator.get.database}
          
          def getAll: List[${entity.toType}] = {
            db.withSession { implicit session =>
              ${entity.dao.obj.toTerm}.getAll.map(row => $rowToEntity)
            }
          }
          
          def get(id: Int): Option[${entity.toType}] = {
            db.withSession { implicit session =>
              ${entity.dao.obj.toTerm}.get(id).map(row => $rowToEntity)
            }
          }
          
          def create(instance: ${entity.toType}): ${entity.toType} = {
            db.withSession { implicit session =>
              val newId = ${entity.dao.obj.toTerm}.create($entityToRow)
              ..${manyToManyCreate(true)}
              instance.copy(id = Some(newId))
            }
          }
          
          def replace(instance: ${entity.toType}): Option[${entity.toType}] = {
            db.withSession { implicit session =>
              if (${entity.dao.obj.toTerm}.replace($entityToRow)) {
                ..$manyToManyDelete
                ..${manyToManyCreate(false)}
                Some(instance)          
              } else {
                None
              }
            }
          }
          
          def delete(id: Int) = {
            db.withSession { implicit session =>
              ${entity.dao.obj.toTerm}.delete(id)
            }
          }
          
          db.withSession { implicit session =>
            ..$createIfNotExists
          }
        }
      """
    }
    
    def declareDaos: List[Tree] = {
      
      q"val ${entity.dao.obj.toTerm} = new ${entity.dao.toType}(${driverGenerator.get.driver})" ::
        entity.manyToManyToImplement.map(property =>
          q"""
            val ${entity.merge(property).dao.obj.toTerm} = 
              new ${entity.merge(property).dao.toType}(${driverGenerator.get.driver})
          """
        )
    }

    def manyToManyCreate(firstTime: Boolean): List[Tree] = {
      val idTerm = if (firstTime) q"newId" else q"instance.id.get"
      
      entity.manyToManyToImplement.map(property =>
        q"""${entity.merge(property).dao.obj.toTerm}.create(instance.${property.name.toTerm}.map(id =>
          ${entity.merge(property).row.toTerm}($idTerm, id)))""")
    }
    
    def manyToManyDelete: List[Tree] = {
      entity.manyToManyToImplement.map(property =>
        q"""${entity.merge(property).dao.obj.toTerm}.${entity.deleteById.toTerm}(instance.id.get)""")
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
          if (property.isImplementedAsRow) {
            q"row.${property.toTerm}"
          } else {
            q"${entity.merge(property).dao.obj.toTerm}.${property.end2.className.getIds.toTerm}(row.id.get)"
          }
        });

      q"${entity.name.toTerm}(..$values)"
    }
    
    def createIfNotExists: List[Tree] = {
      q"${entity.dao.obj.toTerm}.createIfNotExists" :: entity.properties
        .filter(property => property.isImplementedAsManyToMany)
        .map(property => q"${entity.merge(property).dao.obj.toTerm}.createIfNotExists")      
    }
        
    repositoryObject :: repositoryClass :: Nil
  }
}
