package metamorphic.dsl.matcher.operation

import metamorphic.dsl.application._
import metamorphic.dsl.generator.{Dependencies, OperationGenerator, UtilGenerator}
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

class EntityOperationFactory(val entityName: String, val body: Option[Code]) {

  val isAsync = Dependencies.repositoryGenerator.get.isAsync;
  
  def byId(operationId: OperationId)(implicit c: Context): Operation = {
    operationId match {
      case `getAllId` => getAll
      case `getId` => get()
      case `createId` => create()
      case `replaceId` => replace()
      case `deleteId` => delete()
    }
  }

  def getAll(implicit c: Context): Operation = {
    Operation(Get(), Path(basePath, None), None, isAsync,
      resolveBody(OperationGenerator.getAllBody)
    )
  }

  def get(pathParameter: String = "id")(implicit c: Context): Operation = {
    Operation(Get(), Path(basePath, Some(pathParameter)), None, isAsync,
      resolveBody(OperationGenerator.getBody(pathParameter))
    )
  }

  def create(contentName: String = "instance")(implicit c: Context): Operation = {
    Operation(Post(), Path(basePath, None), Some(Content(entityName, contentName)), isAsync,
      resolveBody(OperationGenerator.createBody(contentName))
    )
  }

  def replace(contentName: String = "instance", pathParameter: String = "id")(implicit c: Context): Operation = {
    val copy = "metamorphicInstanceCopy";
    val start = OperationGenerator.replaceStart(contentName, pathParameter, copy)
    val body = start.merge(resolveBody(OperationGenerator.replaceBody(contentName)))

    Operation(Put(), Path(basePath, Some(pathParameter)), Some(Content(entityName, copy)), isAsync, body)
  }

  def delete(pathParameter: String = "id")(implicit c: Context): Operation = {
    Operation(Delete(), Path(basePath, Some(pathParameter)), None, isAsync,
      resolveBody(OperationGenerator.deleteBody(pathParameter))
    )
  }

  def basePath: String = {
    entityName.plural.lowerFirst
  }

  def resolveBody(defaultBody: Code)(implicit c: Context): Code = {
    import c.universe._
    
    def transformSuperReference(operationId: OperationId, args: List[Tree]): Tree = {

      def parameter: String = {
        args.head.toString
      }

      val result = operationId.toString match {
        case `getAllId` => OperationGenerator.getAllBody
        case `getId` => OperationGenerator.getBody(parameter)
        case `createId` => OperationGenerator.createBody(parameter)
        case `replaceId` => OperationGenerator.replaceBody(parameter)
        case `deleteId` => OperationGenerator.deleteBody(parameter)
      }
      
      q"..${result.trees}"
    }

    val resolvedBody = body match {
      case Some(body) => body
      case None => defaultBody
    }
    
    val trees = resolvedBody.trees
      .map(tree => UtilGenerator.transformByPattern(c)(tree, {
        case q"super.$operationId(..$args)" => transformSuperReference(operationId.toString, args)
        case q"super.$operationId" => transformSuperReference(operationId.toString, Nil)
      }))

    Code(trees)
  }
}
