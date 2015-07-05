package metamorphic.dsl

import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

package object model {

  implicit class EntityImplicits(entity: Entity) {

    def id: String = entity.name.id
    def obj: String = entity.name.obj
    def repository: String = entity.name.repository
    def service: String = entity.name.service
    
    def merge(property: Property): String = {
      entity.name + property.name.upperFirst
    }
    
    def toType(implicit c: Context): c.universe.TypeName = {
      entity.name.toType
    }
    
    def toTerm(implicit c: Context): c.universe.TermName = {
      entity.name.toTerm
    }
  }
  
  implicit class PropertyImplicits(property: Property) {
    
    def toTerm(implicit c: Context): c.universe.TermName = {
      property.name.toTerm
    }

    def primitiveType(implicit c: Context): c.Type = {
      import c.universe._
      
      if (property.options.isOption) {
        property.tpe match {
          case MString() => typeOf[Option[String]]
          case MInteger() => typeOf[Option[Int]]
          case MDouble() => typeOf[Option[Double]]
          case MBoolean() => typeOf[Option[Boolean]]
          case MDate() => typeOf[Option[org.joda.time.LocalDate]]
          case MDateTime() => typeOf[Option[org.joda.time.DateTime]]
          case MRelation(end1, _, _) => {
            end1 match {
              case ObjectEnd(_) => typeOf[Option[Int]]
              case ListEnd(_) => typeOf[List[Int]]
            }
          }
        }
      } else {
        property.tpe match {
          case MString() => typeOf[String]
          case MInteger() => typeOf[Int]
          case MDouble() => typeOf[Double]
          case MBoolean() => typeOf[Boolean]
          case MDate() => typeOf[org.joda.time.LocalDate]
          case MDateTime() => typeOf[org.joda.time.DateTime]
          case MRelation(end1, _, _) => {
            end1 match {
              case ObjectEnd(_) => typeOf[Int]
              case ListEnd(_) => typeOf[List[Int]]
            }
          }
        }
      }
    }
  }
}
