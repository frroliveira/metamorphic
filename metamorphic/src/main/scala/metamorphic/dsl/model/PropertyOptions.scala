package metamorphic.dsl.model

trait PropertyOptions {
  val isOption: Boolean
  val isUnique: Boolean
  val isVisible: Boolean
}

case class BasePropertyOptions(
  override val isOption: Boolean,
  override val isUnique: Boolean,
  override val isVisible: Boolean) extends PropertyOptions

case class RelationPropertyOptions(
  override val isOption: Boolean,
  override val isUnique: Boolean,
  override val isVisible: Boolean,
  isComposition: Boolean,
  isReverse: Boolean) extends PropertyOptions