package metamorphic.dsl.matcher.service

import metamorphic.dsl.application.Service
import metamorphic.dsl.matcher.operation._

import scala.reflect.macros.blackbox.Context

object ServiceMatcher {

  def eval(c: Context)(stmt: c.Tree, globalDefaultList: OperationList): Option[(String, Service)] = {
    import c.universe._
    
    val (clasz, typeClass, stmts): (TypeName, Tree, List[Tree]) = {
      stmt match {
        case q"class $clasz extends EntityService[$typeClass] { ..$stmts }" => (clasz, typeClass, stmts)
        case _ => (null, null, null)
      }
    }
    
    def entityName: String = {
      typeClass.toString
    }
    
    def serviceName: String = {
      clasz.toString
    }
    
    def isValid: Boolean = {
      clasz != null
    }

    def serviceDefaultList: OperationList = {
      val serviceList = OperationListMatcher.evalList(c)(stmts)

      if (serviceList.length == 0) globalDefaultList
      else serviceList
    }

    def service: Service = {
      val explicitOperations = EntityOperationMatcher.evalList(c)(stmts, entityName)
      ServiceFactory.entityService(serviceName, entityName, serviceDefaultList, explicitOperations)(c)
    }
    
    if (isValid) Some((entityName, service))
    else None
  }

  def evalList(c: Context)(stmts: List[c.Tree], globalDefaultList: OperationList): List[(String, Service)] = {

    stmts
      .map(stmt => eval(c)(stmt, globalDefaultList))
      .collect {
        case Some(service) => service
      }
  }
}
