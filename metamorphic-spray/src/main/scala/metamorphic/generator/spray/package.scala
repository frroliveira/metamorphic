package metamorphic.generator

import metamorphic.dsl.model._

package object spray {
  
  implicit class SprayStringImplicits(s: String) {
    
    def route: String = s + "Route"
    def actor: String = s + "Actor"
  }
    
  implicit class SprayEntityImplicits(entity: Entity) {

    def route: String = entity.name.route
  }
}
