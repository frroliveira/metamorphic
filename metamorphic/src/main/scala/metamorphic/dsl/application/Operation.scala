package metamorphic.dsl.application

case class Operation(
  method: Method,
  path: Path,
  content: Option[Content],
  isAsync: Boolean,
  body: Code) {
  
  def changeBody(body: Code): Operation = {
    Operation(method, path, content, isAsync, body)
  }
}
