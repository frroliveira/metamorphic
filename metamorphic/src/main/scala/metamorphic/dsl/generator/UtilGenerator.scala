package metamorphic.dsl.generator

import metamorphic.dsl.model._

import scala.reflect.macros.blackbox.Context

object UtilGenerator {

  def modelCaseClasses(model: Model)(implicit c: Context): List[c.Tree] = {
    import c.universe._
    
    def caseClass(entity: Entity): Tree = {
      val typeName = TypeName(entity.name)
      val stmts = q"id: Option[Int]" :: entity.properties
          .filter(property => property.options.isVisible)
          .map(property => valDef(property))

      q"case class $typeName (..$stmts)"
    }
    
    model.entities.map(entity => caseClass(entity))
  }
  
  def valDef(property: Property)(implicit c: Context): c.Tree = {
    import c.universe._
    q"val ${property.toTerm}: ${property.primitiveType}"
  }
  
  def recursiveMethod[T](list: List[T], application: RecursiveApplication[T])(implicit c: Context): c.Tree = {
    import c.universe._
    
    def generate(list: List[T]): Tree = {
      
      list match {
        case value :: Nil => application.apply(value)
        case value :: tail => {
          Apply(Select(generate(tail), TermName(application.method)), List(application.apply(value)))
        }
        case Nil => {
          c.abort(c.enclosingPosition, application.nilError)
        }
      }
    }
    
    generate(list)
  }
  
  def transformByPattern(c: Context)(tree: c.Tree, pattern: PartialFunction[c.Tree, c.Tree]): c.Tree = {
    import c.universe._
    
    new Transformer {
      override def transform(subtree: Tree): Tree = {
        if (pattern isDefinedAt subtree) pattern.apply(subtree)
        else super.transform(subtree)
      }
    }.transform(tree.duplicate)
  }
  
  def transformListByPattern(c: Context)(trees: List[c.Tree], pattern: PartialFunction[List[c.Tree], List[c.Tree]]): List[c.Tree] = {
    import c.universe._
    
    new Transformer {
      override def transformTrees(trees: List[Tree]): List[Tree] = {
        if (pattern isDefinedAt trees) pattern.apply(trees)
        else super.transformTrees(trees)
      }
    }.transformTrees(trees.map(tree => tree.duplicate))
  }
}

trait RecursiveApplication[T] {
  
  def method: String
  def nilError: String
  def apply(value: T)(implicit c: Context): c.Tree 
}
