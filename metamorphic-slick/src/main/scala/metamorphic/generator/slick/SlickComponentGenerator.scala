package metamorphic.generator.slick

import metamorphic.dsl.model._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

class SlickComponentGenerator(entity: Entity) {

  def generate(implicit c: Context): List[c.Tree] = {
    import c.universe._
          
    entityComponent :: entity.manyToManyToImplement.map(property => manyToManyComponent(property))
  }
  
  def entityComponent(implicit c: Context): c.Tree = {
    import c.universe._
    
    def references: List[TypeName] = {
      entity.keysToImplement.map(property => property.end2.className.component.toType).distinct
    }

    def idColumn: DefDef = {
      q"""def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)"""
    }

    def normalColumn(property: Property): DefDef = {
      q"""def ${property.toTerm} = column[${property.primitiveType}](${property.name.toUpperCase})"""
    }
    
    def universalColumn: DefDef = {
      val tuple = q"id.?" :: entity.properties
        .filter(property => property.isImplementedAsRow)
        .map(property => 
          if (property.isImplementedAsKey) q"${property.name.id.toTerm}"
          else q"${property.toTerm}")
      
      if (tuple.length == 1) {
        q"def * = id.? <> (${entity.row.toTerm}, ${entity.row.toTerm}.unapply _)"
      } else {
        q"def * = (..$tuple) <> (${entity.row.toTerm}.tupled, ${entity.row.toTerm}.unapply)"
      }
    }
    
    def foreignKey(property: Property): List[DefDef] = {
      q"""def ${property.name.id.toTerm} = column[Int](${property.name.id.toUpperCase})""" ::
        q"""def ${property.toTerm} = foreignKey(${property.name.toUpperCase}, ${property.name.id.toTerm}, ${property.end2.className.table.toTerm})(_.id, onDelete = ForeignKeyAction.Cascade)""" ::
          Nil
    }
      
    def columns: List[DefDef] = {
      idColumn :: universalColumn :: entity.properties.flatMap(property => 
        if (property.isBaseProperty) normalColumn(property) :: Nil
        else if (property.isImplementedAsKey) foreignKey(property)
        else Nil
      )
    }
    
    q"""
      trait ${entity.component.toType} extends Profile with ..$references {
        import driver.simple._
        import jodaSupport._
        
        class ${entity.table.toType}(tag: Tag) extends
            Table[${entity.row.toType}](tag, ${entity.name.plural}) {
          ..$columns
        }
        
        object ${entity.table.toTerm} extends TableQuery(new ${entity.table.toType}(_))
      }
    """
  }
  
  def manyToManyComponent(property: Property)(implicit c: Context): c.Tree = {
    import c.universe._
    
    val merge = entity.merge(property)
    val end1Class = property.end1.className
    val end2Class = property.end2.className
    val index1Name = merge + "Idx1"
    val index2Name = merge + "Idx2"

    q"""
      trait ${merge.component.toType} extends Profile 
          with ${end1Class.component.toType}
          with ${end2Class.component.toType} {
        import driver.simple._
        
        class ${merge.table.toType}(tag: Tag) extends
            Table[${merge.row.toType}](tag, $merge) {
          def ${end1Class.id.toTerm} = column[Int](${end1Class.id.toUpperCase})
          def ${end2Class.id.toTerm} = column[Int](${end2Class.id.toUpperCase})
          def * = (${end1Class.id.toTerm}, ${end2Class.id.toTerm}) <> (${merge.row.toTerm}.tupled, ${merge.row.toTerm}.unapply)
          def ${end1Class.obj.toTerm} = foreignKey(${end1Class.toUpperCase}, ${end1Class.id.toTerm}, ${end1Class.table.toTerm})(_.id, onDelete = ForeignKeyAction.Cascade)
          def ${end2Class.obj.toTerm} = foreignKey(${end2Class.toUpperCase}, ${end1Class.id.toTerm}, ${end1Class.table.toTerm})(_.id, onDelete = ForeignKeyAction.Cascade)
          def idx1 = index($index1Name, ${end1Class.id.toTerm})
          def idx2 = index($index2Name, ${end2Class.id.toTerm})
        }
        
        object ${merge.table.toTerm} extends TableQuery(new ${merge.table.toType}(_))
      }
    """
  }
}
