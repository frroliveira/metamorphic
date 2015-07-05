package metamorphic.dsl.model

case class Model(entities: List[Entity]) {
  
  def entity(entityName: String): Option[Entity] = {
    entities.find(entity => entity.name.equals(entityName))
  }
}