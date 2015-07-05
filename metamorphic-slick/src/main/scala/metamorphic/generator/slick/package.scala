package metamorphic.generator

import metamorphic.dsl.model._
import metamorphic.dsl.util.StringImplicits._

package object slick {
  
  implicit class SlickStringImplicits(s: String) {
    
    def dao: String = s + "DAO"
    def row: String = s + "Row"
    def component: String = s + "Component"
    def table: String = s + "Table"
    def getList: String = "get" + s.plural
    def getIds: String = "get" + s + "Ids"
    def deleteById: String = "deleteBy" + s + "Id"
  }
  
  implicit class SlickEntityImplicits(entity: Entity) {
    
    def dao: String = entity.name.dao
    def row: String = entity.name.row
    def component: String = entity.name.component
    def table: String = entity.name.table
    def getList: String = entity.name.getList
    def getIds: String = entity.name.getIds
    def deleteById: String = entity.name.deleteById

    def keysToImplement: List[Property] = {
      entity.properties.filter(property => property.isImplementedAsKey)
    }
    
    def manyToManyToImplement: List[Property] = {
      entity.properties.filter(property => property.isImplementedAsManyToMany)
    }
  }
  
  implicit class SlickPropertyImplicits(property: Property) {

    def isImplementedAsKey: Boolean = {
      property.isObjectProperty && !property.isReverse
    }
    
    def isImplementedAsManyToMany: Boolean = {
      property.isManyToManyRelation && !property.isReverse
    }
    
    def isManyToManyRelation: Boolean = {
      property.tpe match {
        case MRelation(_: ListEnd, _: ListEnd, _) => true
        case _ => false
      }
    }
    
    def isImplementedAsRow: Boolean = {
      property.isBaseProperty || isImplementedAsKey
    }
  }
}
