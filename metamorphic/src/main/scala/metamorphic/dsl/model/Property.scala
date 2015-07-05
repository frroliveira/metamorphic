package metamorphic.dsl.model

case class Property(
  name: String,
  tpe: PropertyType,
  options: PropertyOptions) {
  
  def isBaseProperty: Boolean = {
    options match {
      case _: BasePropertyOptions => true
      case _ => false
    }
  }
  
  def isObjectProperty: Boolean = {
    tpe match {
      case MRelation(ObjectEnd(_), _, _) => true
      case _ => false
    }
  }
  
  def isListProperty: Boolean = {
    tpe match {
      case MRelation(ListEnd(_), _, _) => true
      case _ => false
    }    
  }
  
  def isReverse: Boolean = {
    options match {
      case options: RelationPropertyOptions => options.isReverse
      case _ => false
    }
  }
  
  def end1: RelationEnd = {
    tpe match { case MRelation(end1, _, _) => end1 }
  }
  
  def end2: RelationEnd = {
    tpe match { case MRelation(_, end2, _) => end2 }    
  }
}