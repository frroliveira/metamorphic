package metamorphic.dsl.model.builder

import metamorphic.dsl.model.Entity
import metamorphic.dsl.util.Attachable

class EntityBuilder(val entityName: String) extends Attachable {

  private var builders: List[PropertyBuilder] = Nil

  def build: Entity = {
    Entity(entityName, propertyBuilders.map(builder => builder.build))
  }

  def propertyBuilders: List[PropertyBuilder] = {
    builders.reverse
  }

  def builder(propertyName: String): Option[PropertyBuilder] = {
    builders.find(builder => builder.propertyName.equals(propertyName))
  }

  def add(propertyName: String): PropertyBuilder = {
    val builder = new PropertyBuilder(propertyName)
    add(builder)
    builder
  }

  def add(propertyBuilder: PropertyBuilder): EntityBuilder = {
    builders = propertyBuilder :: builders
    this
  }

  def add(propertyBuilders: List[PropertyBuilder]): EntityBuilder = {
    propertyBuilders.foreach(builder => add(builder))
    this
  }
}
