package metamorphic.dsl

object Composition {}
object Unique {}
object R {
  object Object {}
}
object Option {}

trait Field {}

case class IntegerField(any: Any*) extends Field
case class DoubleField(any: Any*) extends Field
case class StringField(any: Any*) extends Field
case class BooleanField(any: Any*) extends Field
case class DateField(any: Any*) extends Field
case class DateTimeField(any: Any*) extends Field

trait InvertibleField extends Field {}

case class ObjectField(any: Any*) extends InvertibleField
case class ListField(any: Any*) extends InvertibleField
case class ReverseField(field: InvertibleField, any: Any*) extends Field
