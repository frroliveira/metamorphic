package metamorphic.dsl.matcher

import metamorphic.dsl.application._
import metamorphic.dsl.matcher.model._
import metamorphic.dsl.matcher.operation._
import metamorphic.dsl.matcher.service._
import metamorphic.dsl.model.Model
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

object ApplicationMatcher {
  
  def eval(c: Context)(appTree: c.Tree, model: Model): Application = {
    import c.universe._
    
    val (app, stmts): (TermName, List[Tree]) = {
      appTree match {
        case q"object $app { ..$stmts }" => (app, stmts)
        case _ => (null, null)
      }
    }
    
    def isValid: Boolean = {
      app != null
    }
    
    def globalDefaultList: OperationList = {
      OperationListMatcher.evalList(c)(stmts) match {
        case Nil => OperationList.full
        case operationList => operationList
      }
    }
    
    def application: Application = {
      val explicitServices = ServiceMatcher.evalList(c)(stmts, globalDefaultList)
      
      val services = model.entities
        .map(entity => (entity, explicitServices.find(_._1.equals(entity.name))))
        .collect {
          case (_, Some((_, service))) => {
            service
          }
          case (entity, None) => {
            ServiceFactory.entityService(entity.name.service, entity.name, globalDefaultList)(c)
          }
        }

      Application(app.toString, services)
    }
    
    if (isValid) application
    else c.abort(appTree.pos, "Unexpected app usage.")
  }
  
  def content(c: Context)(tree: c.Tree): List[c.Tree] = {
    import c.universe._

    tree match {
      case q"object $app { ..$stmts }" => stmts
      case _ => Nil
    }
  }
}
