package metamorphic.generator.slick

import metamorphic.dsl.model._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

class SlickDaoGenerator(entity: Entity) {

  def generate(implicit c: Context): List[c.Tree] = {
    import c.universe._
   
    entityDao :: entity.manyToManyToImplement
      .map(property => manyToManyDao(property))
  }
  
  private def entityDao(implicit c: Context): c.Tree = {          
    import c.universe._

    q"""
      class ${entity.dao.toType}(override val driver: JdbcDriver) extends ${entity.component.toType} {
        import driver.simple._
        import scala.slick.jdbc.meta.MTable      

        private val autoInc = ${entity.table.toTerm} returning ${entity.table.toTerm}.map(_.id)
        private val findById = ${entity.table.toTerm}.findBy(_.id)
      
        def createIfNotExists()(implicit session: Session) = {
          if (!exists) ${entity.table.toTerm}.ddl.create
        }
        
        def exists() (implicit session: Session): Boolean = {
          MTable.getTables(${entity.name.plural}).list(session).nonEmpty
        }
          
        def getAll()(implicit session: Session): List[${entity.row.toType}] = {
          ${entity.table.toTerm}.list
        }
        
        def get(id: Int)(implicit session: Session): Option[${entity.row.toType}] = {
          findById(id).firstOption
        }
        
        def create(value: ${entity.row.toType})(implicit session: Session): Int = {
          autoInc += value
        }
        
        def replace(value: ${entity.row.toType})(implicit session: Session): Boolean = {
          findById(value.id.get).update(value) == 1
        }
        
        def delete(id: Int)(implicit session: Session): Boolean = {
          findById(id).delete == 1
        }
      }
    """
  }
  
  private def manyToManyDao(property: Property)(implicit c: Context): c.Tree = {
    import c.universe._
    
    val merge = entity.merge(property)
    val end1Class = property.end1.className
    val end2Class = property.end2.className
    
    q"""
      class ${merge.dao.toType}(override val driver: JdbcDriver) extends ${merge.component.toType} {
        import driver.simple._
        import scala.slick.jdbc.meta.MTable      
       
        private val findByEnd1Id = ${merge.table.toTerm}.findBy(_.${end1Class.id.toTerm})        
        private val findByEnd2Id = ${merge.table.toTerm}.findBy(_.${end2Class.id.toTerm})

        private val getIdsEnd1 = Compiled((id: Column[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end2Class.id.toTerm} === id)
            .map(_.${end1Class.id.toTerm})
        )

        private val getIdsEnd2 = Compiled((id: Column[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end1Class.id.toTerm} === id)
            .map(_.${end2Class.id.toTerm})
        )
        
        private val getListEnd1 = Compiled((id: Column[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end2Class.id.toTerm} === id)
            .join(${end1Class.table.toTerm})
            .on(_.${end1Class.id.toTerm} === _.id)
            .map(_._2)
        )

        private val getListEnd2 = Compiled((id: Column[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end1Class.id.toTerm} === id)
            .join(${end2Class.table.toTerm})
            .on(_.${end2Class.id.toTerm} === _.id)
            .map(_._2)
        )
        
        def createIfNotExists()(implicit session: Session) = {
          if (!exists) ${merge.table.toTerm}.ddl.create
        }
        
        def exists() (implicit session: Session): Boolean = {
          MTable.getTables($merge).list(session).nonEmpty
        }
        
        def ${end1Class.getIds.toTerm}(id: Int)(implicit session: Session): List[Int] = {
          getIdsEnd1(id).run.toList
        }
        
        def ${end2Class.getIds.toTerm}(id: Int)(implicit session: Session): List[Int] = {
          getIdsEnd2(id).run.toList
        }
        
        def ${end1Class.getList.toTerm}(id: Int)(implicit session: Session): List[${end1Class.row.toType}] = {
          getListEnd1(id).run.toList
        }
        
        def ${end2Class.getList.toTerm}(id: Int)(implicit session: Session): List[${end2Class.row.toType}] = {
          getListEnd2(id).run.toList
        }
        
        def create(ids: List[${merge.row.toType}])(implicit session: Session) = {
          ${merge.table.toTerm} ++= ids
        }
        
        def ${end1Class.deleteById.toTerm}(id: Int)(implicit session: Session): Int = {
          findByEnd1Id(id).delete
        }
      
        def ${end2Class.deleteById.toTerm}(id: Int)(implicit session: Session): Int = {
          findByEnd2Id(id).delete
        }
      }
    """
  }
}
