package metamorphic.generator.slick

import metamorphic.dsl.model._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

class SlickAsyncDaoGenerator(entity: Entity) {

  def generate(implicit c: Context): List[c.Tree] = {
    import c.universe._
   
    entityDao :: entity.manyToManyToImplement
      .map(property => manyToManyDao(property))
  }
  
  private def entityDao(implicit c: Context): c.Tree = {          
    import c.universe._

    q"""
      class ${entity.dao.toType}(override val driver: JdbcDriver) extends ${entity.component.toType} {
        import driver.api._
        import slick.jdbc.meta.MTable

        private val findById = Compiled((id: Rep[Int]) =>
          ${entity.table.toTerm}.filter(_.id === id)
        )
      
        def createSchema = {
          ${entity.table.toTerm}.schema.create
        }
        
        def table = {
          MTable.getTables(${entity.name.plural}).headOption
        }
        
        def getAll = {
          ${entity.table.toTerm}.result
        }
        
        def get(id: Int) = {
          findById(id).result.headOption
        }
        
        def create(instance: ${entity.row.toType}) = {
          ${entity.table.toTerm} returning ${entity.table.toTerm}.map(_.id) += instance
        }
        
        def replace(instance: ${entity.row.toType}) = {
          findById(instance.id.get).update(instance)
        }
        
        def delete(id: Int) = {
          findById(id).delete
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
        import driver.api._
        import slick.jdbc.meta.MTable
       
        private val findByEnd1Id = Compiled((id: Rep[Int]) =>
          ${merge.table.toTerm}.filter(_.${end1Class.id.toTerm} === id)
        )
        
        private val findByEnd2Id = Compiled((id: Rep[Int]) =>
          ${merge.table.toTerm}.filter(_.${end2Class.id.toTerm} === id)
        )

        private val getIdsEnd1 = Compiled((id: Rep[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end2Class.id.toTerm} === id)
            .map(_.${end1Class.id.toTerm})
        )

        private val getIdsEnd2 = Compiled((id: Rep[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end1Class.id.toTerm} === id)
            .map(_.${end2Class.id.toTerm})
        )
        
        private val getListEnd1 = Compiled((id: Rep[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end2Class.id.toTerm} === id)
            .join(${end1Class.table.toTerm})
            .on(_.${end1Class.id.toTerm} === _.id)
            .map(_._2)
        )

        private val getListEnd2 = Compiled((id: Rep[Int]) =>
          ${merge.table.toTerm}
            .filter(_.${end1Class.id.toTerm} === id)
            .join(${end2Class.table.toTerm})
            .on(_.${end2Class.id.toTerm} === _.id)
            .map(_._2)
        )
        
        def createSchema = {
          ${merge.table.toTerm}.schema.create
        }
        
        def table = {
          MTable.getTables($merge).headOption
        }
        
        def ${end1Class.getIds.toTerm}(id: Int) = {
          getIdsEnd1(id).result
        }
        
        def ${end2Class.getIds.toTerm}(id: Int) = {
          getIdsEnd2(id).result
        }
        
        def ${end1Class.getList.toTerm}(id: Int) = {
          getListEnd1(id).result
        }
        
        def ${end2Class.getList.toTerm}(id: Int) = {
          getListEnd2(id).result
        }
        
        def create(ids: List[${merge.row.toType}]) = {
          ${merge.table.toTerm} ++= ids
        }
        
        def ${end1Class.deleteById.toTerm}(id: Int) = {
          findByEnd1Id(id).delete
        }
      
        def ${end2Class.deleteById.toTerm}(id: Int) = {
          findByEnd2Id(id).delete
        }
      }
    """
  }
}
