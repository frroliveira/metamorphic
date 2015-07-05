package metamorphic.dsl.matcher.operation

import scala.reflect.macros.blackbox.Context

object OperationListMatcher {

  def eval(c: Context)(stmt: c.Tree): OperationList = {
    import c.universe._
    
    stmt match {
      case q"val operations = List(..$operations)" => {
        operations.map(operation =>
          operation.toString match {
            case "GetAll" => getAllId
            case "Get" => getId
            case "Create" => createId
            case "Replace" => replaceId
            case "Delete" => deleteId
            case _ => c.abort(stmt.pos, s"Invalid operation '${operation.toString}'.")
          }
        )
      }
      case _ => Nil
    }
  }

  def evalList(c: Context)(stmts: List[c.Tree]): OperationList = {

    stmts.flatMap(stmt => eval(c)(stmt))
  }
}
