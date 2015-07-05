package metamorphic.dsl.matcher

import metamorphic.dsl._
import metamorphic.dsl.model._
import metamorphic.dsl.util.ModelProvider

import org.scalatest.FunSuite
import org.scalatest.Matchers

class EntityMatcherSuite extends FunSuite with Matchers {
  
  @entity class Mock0 {}
  @entity class Mock1 {
    def obj = ObjectField(Mock0)
    def list = ListField(Mock0)
  }
  
  test("No entity") {
    val entity = ModelProvider.entity {}
    
    entity should matchPattern { case None => }
  }
    
  test("Empty entity") {
    val entity = ModelProvider.entity {
      @entity class Book {}
    }
    
    entity should matchPattern { case Some(_) => }
    assert(entity.get.name.equals("Book"))
    assert(entity.get.properties.isEmpty)
  }
  
  test("Simple entity") {
    val entity = ModelProvider.entity {
      @entity class Book {
        def name = StringField()
      }
    }
    
    entity should matchPattern { case Some(_) => }
    assert(entity.get.name.equals("Book"))
    assert(entity.get.properties.length == 1)
  }
  
  test("Larger entity") {
    val entity = ModelProvider.entity {
      @entity class Book {
        def name = StringField()
        def copies = IntegerField()
        def price = DoubleField()
      }
    }
    
    entity should matchPattern { case Some(_) => }
    assert(entity.get.name.equals("Book"))
    assert(entity.get.properties.length == 3)
  }
}
