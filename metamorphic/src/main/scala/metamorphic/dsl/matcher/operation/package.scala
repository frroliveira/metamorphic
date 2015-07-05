package metamorphic.dsl.matcher

import metamorphic.dsl.application.Operation

package object operation {

  type OperationId = String
  type OperationList = List[OperationId]
  type OperationPair = (OperationId, Operation)
  type OperationMap = List[OperationPair]
  
  val getAllId: OperationId = "getAll"
  val getId: OperationId = "get"
  val createId: OperationId = "create"
  val replaceId: OperationId = "replace"
  val deleteId: OperationId = "delete"

  object OperationList {

    val full = List(getAllId, getId, createId, replaceId, deleteId)
  }
}
