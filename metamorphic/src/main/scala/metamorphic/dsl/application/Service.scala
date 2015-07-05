package metamorphic.dsl.application

case class Service(
  name: String,
  operations: List[Operation],
  dependencies: Code)
