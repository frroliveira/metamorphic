package metamorphic.dsl

import metamorphic.Settings
import metamorphic.dsl.generator._
import metamorphic.dsl.matcher._
import metamorphic.dsl.matcher.model._

import scala.annotation.StaticAnnotation
import scala.reflect.macros.blackbox.Context

class app extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro AppImplementation.app
}

class AppImplementation(val context: Context) {
  
  implicit val c: Context = context
  import c.universe._
  
  def app(annottees: c.Expr[Any]*): c.Expr[Any] = {

    validateDependencies

    val appTree = annottees.map(_.tree).head
    
    val model = ModelMatcher.eval(c)(appTree)
    val application = ApplicationMatcher.eval(c)(appTree, model)
    
    if (Settings.macroDebug) {
      println("App expansion: " + application.name)
      println("Model: " + model)
      println("Application: " + application)
    }
    
    if (model.entities.length == 0) {
      c.abort(c.enclosingPosition, s"Unable to generate '${application.name}'. No entities.")
      annottees.head
    } else {
      val result = ApplicationGenerator.generate(model, application)

      if (Settings.macroDebug) {
        println("App result: " + result)
      }

      result
    }
  }
  
  def validateDependencies: Unit = {
    Dependencies.repositoryGenerator match {
      case None => c.abort(c.enclosingPosition, "Repository generator library not found.")
      case Some(_) =>
    }
    
    Dependencies.serviceGenerator match {
      case None => c.abort(c.enclosingPosition, "Service generator library not found.")
      case Some(_) =>
    }
  }
}
