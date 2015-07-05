package metamorphic.dsl.model

trait PropertyType
case class MString() extends PropertyType
case class MDouble() extends PropertyType
case class MInteger() extends PropertyType
case class MBoolean() extends PropertyType
case class MDate() extends PropertyType
case class MDateTime() extends PropertyType
case class MRelation(
  end1: RelationEnd,
  end2: RelationEnd,
  reverseProperty: String) extends PropertyType
