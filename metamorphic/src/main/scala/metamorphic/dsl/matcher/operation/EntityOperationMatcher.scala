package metamorphic.dsl.matcher.operation

import metamorphic.dsl.application._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

object EntityOperationMatcher {

  def eval(c: Context)(stmt: c.Tree, entityName: String): Option[OperationPair] = {
    import c.universe._

    val (function, args, body): (TermName, List[Tree], List[Tree]) = {
      stmt match {
        case q"def $function = { ..$body }" => (function, Nil, body)
        case q"def $function(..$args) = { ..$body }" => (function, args, body)
        case _ => ("".toTerm(c), Nil, Nil)
      }
    }

    val operationId: Option[OperationId] = {
      function.toString match {
        case `getAllId` => Some(getAllId)
        case `getId` => Some(getId)
        case `createId` => Some(createId)
        case `replaceId` => Some(replaceId)
        case `deleteId` => Some(deleteId)
        case _ => None
      }
    }
    
    def isValid: Boolean = {
      operationId match {
        case Some(id) => (id.equals(getAllId) && args.length == 0) || args.length == 1
        case None => false
      }
    }

    def parameter: String = {
      val q"""$_ val $parameter: $_""" = args.head
      parameter.toString
    }

    def operation: Operation = {
      val factory = new EntityOperationFactory(entityName, Some(Code(body)))
      
      operationId.get match {
        case `getAllId` => factory.getAll(c)
        case `getId` => factory.get(parameter)(c)
        case `createId` => factory.create(parameter)(c)
        case `replaceId` => factory.replace(parameter)(c)
        case `deleteId` => factory.delete(parameter)(c)
      }
    }
    
    if (isValid) Some((operationId.get, operation))
    else None
  }

  def evalList(c: Context)(stmts: List[c.Tree], entityName: String): OperationMap = {

    stmts
      .map(stmt => eval(c)(stmt, entityName))
      .collect { case Some(pair) => pair }
  }
}
