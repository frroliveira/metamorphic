package metamorphic.dsl.model

trait RelationEnd {
  
  val className: String
}

case class ObjectEnd(override val className: String) extends RelationEnd
case class ListEnd(override val className: String) extends RelationEnd