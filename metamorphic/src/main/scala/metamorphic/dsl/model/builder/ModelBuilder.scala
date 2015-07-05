package metamorphic.dsl.model.builder

import metamorphic.dsl.model.Model
import metamorphic.dsl.util.Attachable

class ModelBuilder extends Attachable {

  private var builders: List[EntityBuilder] = Nil

  def build: Model = {
    Model(entityBuilders.map(builder => builder.build))
  }

  def entityBuilders: List[EntityBuilder] = {
    builders.reverse
  }

  def builder(entityName: String): Option[EntityBuilder] = {
    builders.find(builder => builder.entityName.equals(entityName))
  }

  def add(entityName: String): EntityBuilder = {
    val builder = new EntityBuilder(entityName)
    add(builder)
    builder
  }

  def add(entityBuilder: EntityBuilder): ModelBuilder = {
    builders = entityBuilder :: builders
    this
  }

  def add(entityBuilders: List[EntityBuilder]): ModelBuilder = {
    entityBuilders.foreach(builder => add(builder))
    this
  }
}
