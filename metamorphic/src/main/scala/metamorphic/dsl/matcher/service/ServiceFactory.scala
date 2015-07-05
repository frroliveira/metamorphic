package metamorphic.dsl.matcher.service

import metamorphic.dsl.application._
import metamorphic.dsl.generator.OperationGenerator
import metamorphic.dsl.matcher.operation._

import scala.reflect.macros.blackbox.Context

object ServiceFactory {

  def entityService(serviceName: String, entityName: String, defaultList: OperationList, 
      explicitOperations: OperationMap = Nil)(implicit c: Context): Service = {
    
    val factory = new EntityOperationFactory(entityName, None)

    val defaultOperations =
      defaultList.map(operationId => {

        val operationPair = explicitOperations.find(_._1.equals(operationId))
        operationPair match {
          case Some(pair) => pair._2
          case None => factory.byId(operationId)
        }
      })

    val operations = explicitOperations.map(_._2) ++ defaultOperations
    Service(serviceName, operations, OperationGenerator.serviceDependencies(entityName))
  }
}
