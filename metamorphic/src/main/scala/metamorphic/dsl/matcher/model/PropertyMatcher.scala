package metamorphic.dsl.matcher.model

import metamorphic.dsl.model._
import metamorphic.dsl.model.builder.PropertyBuilder

import scala.reflect.macros.blackbox.Context

object PropertyMatcher {

  def eval(c: Context)(stmt: c.Tree, entityName: String): Option[Property] = {

    val propertyBuilder = parse(c)(stmt, entityName)
    
    propertyBuilder match {
      case Some(propertyBuilder) => Some(propertyBuilder.build)
      case None => None
    }
  }
  
  def evalList(c: Context)(stmts: List[c.Tree], entityName: String): List[Property] = {
  
    stmts
      .map(stmt => eval(c)(stmt, entityName))
      .collect { case Some(property) => property }
  }
  
  def parse(c: Context)(stmt: c.Tree, entityName: String): Option[PropertyBuilder] = {
    import c.universe._

    def namespaceToField(namespace: Tree): Tree = {
      namespace match {
        case q"metamorphic.dsl.$field.apply" => q"$field"
        case q"metamorphic.dsl.$field" => q"$field"
        case q"dsl.$field" => q"$field"
        case _ => namespace
      }
    }
    
    def matchSymbol(symbol: Symbol, str: String): Boolean = {
      val field0 = symbol.fullName.toString
      val field1 = if (field0.startsWith("metamorphic.")) field0.drop(12) else field0
      val field2 = if (field1.startsWith("dsl.")) field1.drop(4) else field1

      str.equals(field2)
    }
      
    val (property, field, options): (TermName, Tree, List[Tree]) =
     stmt match {
        case q"def $property = $field(..$options)" => (property, namespaceToField(field), options)
        case q"val $property = $field(..$options)" => (property, namespaceToField(field), options)
        case _ => (TermName(""), stmt, List())
      }
    
    val propertyBuilder = new PropertyBuilder(property.toString)
    
    def tpe: Option[PropertyType] = {
      field.toString match {
        case "StringField" => Some(new MString())
        case "DoubleField" => Some(new MDouble())
        case "IntegerField" => Some(new MInteger())
        case "BooleanField" => Some(new MBoolean())
        case "DateField" => Some(new MDate())
        case "DateTimeField" => Some(new MDateTime())
        case "ObjectField" => Some(parseRelation(ObjectEnd(entityName)))
        case "ListField" => Some(parseRelation(ListEnd(entityName)))
        case "ReverseField" => Some(parseReverseRelation)
        case _ => None
      }
    }

    def parseRelation(end1: RelationEnd): MRelation = {
    
      def end2(entityName: String) = {
        if (isObject) ObjectEnd(entityName)
        else ListEnd(entityName)
      }
      
      def isObject: Boolean = {
        options.exists(option =>
          matchSymbol(option.symbol, "R.Object") ||
          matchSymbol(option.symbol, "Composition")
        )
      }
      
      options match {
        case entity :: _ => {
          propertyBuilder.attachment = options.head
          MRelation(end1, end2(trim(entity.toString)), null)
        }
        case _ => c.abort(options.head.pos, s"'${field.toString}' must reference a entity.")
      }
    
    }
    
    def parseReverseRelation: MRelation = {
      options match {
        case q"$entity.$property" :: Nil => {
          propertyBuilder.attachment = options.head
          MRelation(ListEnd(entityName), ListEnd(trim(entity.toString)), property.toString)
        }
        case _ => c.abort(options.head.pos, "ReverseField must reference a entity field.")
      }
    }
    
    def propertyOptions: PropertyOptions = {
      if (isRelation) {
        RelationPropertyOptions(isOption, isUnique, isVisible, isComposition, isReverse)
      } else {
        BasePropertyOptions(isOption, isUnique, isVisible)
      }
    }
    
    def trim(entityName: String): String = {
      entityName.split('.').last
    }

    def isUnique: Boolean = {
      options.exists(option => matchSymbol(option.symbol, "Unique"))
    }

    def isOption: Boolean = {
      options.exists(option => matchSymbol(option.symbol, "Option"))
    }
    
    def isComposition: Boolean = {
      options.exists(option => matchSymbol(option.symbol, "Composition"))
    }
    
    def isVisible: Boolean = {
      stmt match {
        case q"def $_ = $_(..$_)" => true
        case _ => false
      }
    }
    
    def isReverse: Boolean = {
      field.toString.equals("ReverseField")
    }
    
    def isRelation: Boolean = {
      field.toString match {
        case "ObjectField" | "ListField" | "ReverseField" => true
        case _ => false
      }
    }

    tpe match {
      case Some(tpe) => Some(propertyBuilder.update(tpe).update(propertyOptions))
      case None => None
    }  
  }
  
  def parseList(c: Context)(stmts: List[c.Tree], entityName: String): List[PropertyBuilder] = {
    
    stmts
      .map(stmt => parse(c)(stmt, entityName))
      .collect { case Some(builder) => builder }
  }
}
