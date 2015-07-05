package metamorphic.dsl.model.builder

import metamorphic.dsl.model.Property
import metamorphic.dsl.model.PropertyOptions
import metamorphic.dsl.model.PropertyType
import metamorphic.dsl.util.Attachable

class PropertyBuilder(val propertyName: String) extends Attachable {

  var tpe: PropertyType = null
  var options: PropertyOptions = null

  def build: Property = {
    Property(propertyName, tpe, options)
  }

  def update(tpe: PropertyType): PropertyBuilder = {
    this.tpe = tpe
    this
  }

  def update(options: PropertyOptions): PropertyBuilder = {
    this.options = options
    this
  }
}
