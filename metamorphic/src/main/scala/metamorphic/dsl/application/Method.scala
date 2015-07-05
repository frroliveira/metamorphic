package metamorphic.dsl.application

trait Method
case class Get() extends Method
case class Post() extends Method
case class Put() extends Method
case class Delete() extends Method