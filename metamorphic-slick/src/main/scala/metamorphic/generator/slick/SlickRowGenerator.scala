package metamorphic.generator.slick

import metamorphic.dsl.model._
import metamorphic.dsl.generator._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

class SlickRowGenerator(entity: Entity) {

  def generate(implicit c: Context): List[c.Tree] = {
    import c.universe._
    
    def entityRow: Tree = {
      val stmts = q"val id: Option[Int]" :: entity.properties
        .filter(property => property.isImplementedAsRow)
        .map(property => UtilGenerator.valDef(property))
      
      q"case class ${entity.row.toType} (..$stmts)"
    }
    
    def manyToManyRow(property: Property): Tree = {
      q"""
        case class ${entity.merge(property).row.toType} (
          ${property.end1.className.id.toTerm}: Int,
          ${property.end2.className.id.toTerm}: Int
        )
      """
    }
  
    entityRow :: entity.manyToManyToImplement.map(property => manyToManyRow(property))    
  }
}
