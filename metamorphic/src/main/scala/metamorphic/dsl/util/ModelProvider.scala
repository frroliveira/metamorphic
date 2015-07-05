package metamorphic.dsl.util

import metamorphic.dsl.model._
import metamorphic.dsl.model.builder._
import metamorphic.dsl.matcher.model._

import scala.reflect.macros.blackbox.Context

class ModelProviderImpl(override val c: Context) extends ModelLiftables {
  import c.universe._
  
  def childrenHead(tree: Tree): Tree = {
    if (tree.children.isEmpty) tree else tree.children.head
  }
    
  def model[T](code: c.Expr[T]): c.Expr[Model] = {
    val appTree = code.tree.children
    val model = ModelMatcher.evalList(c)(appTree)
    c.Expr[Model](q"$model")
  }
  
  def entity[T](code: c.Expr[T]): c.Expr[Option[Entity]] = {
    val entityTree = childrenHead(code.tree)
    val entity = EntityMatcher.eval(c)(entityTree)    
    c.Expr[Option[Entity]](q"$entity")
  }
  
  def property[T](code: c.Expr[T]): c.Expr[Option[Property]] = {
    val (content, entityName): (Tree, String) = {
      code.tree match {
        case q"(_: $entity) => $tree" => {
          entity match {
            case entity: TypeTree => (tree, entity.symbol.name.toString)
          }
        }
        case tree => (tree, "")
      }
    }
    
    val propertyTree = childrenHead(content)
    val property = PropertyMatcher.eval(c)(propertyTree, entityName)
    c.Expr[Option[Property]](q"$property")
  }
}

object ModelProvider {
  
  def model[T](code: => T): Model = macro ModelProviderImpl.model[T]
  def entity[T](code: => T): Option[Entity] = macro ModelProviderImpl.entity[T]
  def property[T](code: => T): Option[Property] = macro ModelProviderImpl.property[T]
}
