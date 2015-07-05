package metamorphic.dsl.matcher

import metamorphic.dsl._
import metamorphic.dsl.model._
import metamorphic.dsl.util.ModelProvider

import org.scalatest.FunSuite
import org.scalatest.Matchers

class ModelMatcherSuite extends FunSuite with Matchers {
  
  @entity class Mock0 {}
  @entity class Mock1 {
    def obj = ObjectField(Mock0)
    def list = ListField(Mock0)
  }

  test("Empty model") {
    val model = ModelProvider.model {}
    
    assert(model.entities.isEmpty)
  }
  
  test("Simple model") {
    val model = ModelProvider.model {
      @entity class Book {
        def name = StringField()
      }
    }
    
    assert(model.entities.length == 1)
    assert(model.entities.head.name.equals("Book"))
  }
  
  test("Reverse of Object to List relation") {
    val model = ModelProvider.model {
      @entity class Book {
        def library = ObjectField(Library)
      }
 
      @entity class Library {
        def books = ReverseField(Book.library)
      }
    }

    assert(model.entities.length == 2)

    val book = model.entity("Book")
    val property = book.get.properties.head

    property.tpe should matchPattern { case MRelation(ObjectEnd("Book"), ListEnd("Library"), "books") => }
    property.options should matchPattern { case RelationPropertyOptions(false, false, true, false, false) => }

    val library = model.entity("Library")
    val reverseProperty = library.get.properties.head
    
    reverseProperty.tpe should matchPattern { case MRelation(ListEnd("Library"), ObjectEnd("Book"), "library") => }
    reverseProperty.options should matchPattern { case RelationPropertyOptions(false, false, true, false, true) => }
  }

  test("Reverse of List to List relation") {
    val model = ModelProvider.model {
      @entity class Book {
        def categories = ListField(Category)
      }
      
      @entity class Category {
        def books = ReverseField(Book.categories)
      }
    }
    
    assert(model.entities.length == 2)
 
    val book = model.entity("Book")
    val property = book.get.properties.head

    property.tpe should matchPattern { case MRelation(ListEnd("Book"), ListEnd("Category"), "books") => }
    property.options should matchPattern { case RelationPropertyOptions(false, false, true, false, false) => }

    val category = model.entity("Category")
    val reverseProperty = category.get.properties.head
    
    reverseProperty.tpe should matchPattern { case MRelation(ListEnd("Category"), ListEnd("Book"), "categories") => }
    reverseProperty.options should matchPattern { case RelationPropertyOptions(false, false, true, false, true) => }
  }
}
