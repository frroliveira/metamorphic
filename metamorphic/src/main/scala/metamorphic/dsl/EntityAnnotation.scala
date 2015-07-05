package metamorphic.dsl

import metamorphic.Settings

import scala.annotation.StaticAnnotation
import scala.reflect.macros.blackbox.Context

class entity extends StaticAnnotation {
  
  def macroTransform(annottees: Any*): Any = macro EntityImplementation.entity
}

class EntityImplementation(val c: Context) {

  def entity(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val clasz = annottees.map(_.tree).toList.head
    val q"class $name { ..$body }" = clasz
    val classAndObject: List[Tree] = clasz :: q"object ${name.toTermName} { ..$body }" :: Nil
    val result = c.Expr(q"..$classAndObject")

    if (Settings.macroDebug) {
      println("Entity expansion: " + name.toString)
      println("Entity result: " + result)
    }
    
    result
  }
}
