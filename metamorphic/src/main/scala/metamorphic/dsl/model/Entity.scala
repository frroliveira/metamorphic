package metamorphic.dsl.model

case class Entity(
  name: String,
  properties: List[Property]) {
  
  def property(propertyName: String): Option[Property] = {
    properties.find(property => property.name.equals(propertyName))
  }
}