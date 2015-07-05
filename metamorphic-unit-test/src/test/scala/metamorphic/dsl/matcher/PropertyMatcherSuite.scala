package metamorphic.dsl.matcher

import metamorphic.dsl._
import metamorphic.dsl.model._
import metamorphic.dsl.util.ModelProvider

import org.scalatest.FunSuite
import org.scalatest.Matchers

class PropertyMatcherSuite extends FunSuite with Matchers {
  
  @entity class Mock0 {}
  @entity class Mock1 {
    def obj = ObjectField(Mock0)
    def list = ListField(Mock0)
  }

  test("No field") {
    val property = ModelProvider.property { }
    
    property should matchPattern { case None => }
  }

  test("Invalid field") {
    """
    ModelProvider.property {
      def name = TextField()
    }
    """ shouldNot compile
  }
  
  test("Simple field") {
    val property = ModelProvider.property {
      def count = IntegerField()
    }
    
    property should matchPattern { case Some(_) => }
    property.get.name shouldEqual "count"
    property.get.options should matchPattern { case BasePropertyOptions(false, false, true) => }    
  }

  test("Option field") {
    val property = ModelProvider.property {
      def isOk = BooleanField(Option)
    }
    
    property.get.options should matchPattern { case BasePropertyOptions(true, false, true) => }    
  }

  test("Simple StringField") {
    val property = ModelProvider.property {
      def name = StringField()
    }
    
    property.get.tpe should matchPattern { case MString() => }
  }
  
  test("Simple DoubleField") {
    val property = ModelProvider.property {
      def name = DoubleField()
    }
    
    property.get.tpe should matchPattern { case MDouble() => }
  }
  
  test("Simple IntegerField") {
    val property = ModelProvider.property {
      def name = IntegerField()
    }
    
    property.get.tpe should matchPattern { case MInteger() => }
  }

  test("Simple BooleanField") {
    val property = ModelProvider.property {
      def name = BooleanField()
    }
    
    property.get.tpe should matchPattern { case MBoolean() => }
  }

  test("Simple DateField") {
    val property = ModelProvider.property {
      def name = DateField()
    }
    
    property.get.tpe should matchPattern { case MDate() => }
  }

  test("Simple DateTimeField") {
    val property = ModelProvider.property {
      def name = DateTimeField()
    }
    
    property.get.tpe should matchPattern { case MDateTime() => }
  }
  
  test("Object to List relation") {
    val property = ModelProvider.property { _: Mock0 =>
      def obj = ObjectField(Mock1)
    }
    
    property.get.tpe should matchPattern { case MRelation(ObjectEnd("Mock0"), ListEnd("Mock1"), null) => }
    property.get.options should matchPattern { case RelationPropertyOptions(false, false, true, false, false) => }
  }

  test("Object to Object relation") {
    val property = ModelProvider.property { _: Mock0 =>
      def obj = ObjectField(Mock1, R.Object)
    }
    
    property.get.tpe should matchPattern { case MRelation(ObjectEnd("Mock0"), ObjectEnd("Mock1"), null) => }
    property.get.options should matchPattern { case RelationPropertyOptions(false, false, true, false, false) => }
  }

  test("List to Object relation") {
    val property = ModelProvider.property { _: Mock0 =>
      def list = ListField(Mock1, R.Object)
    }
    
    property.get.tpe should matchPattern { case MRelation(ListEnd("Mock0"), ObjectEnd("Mock1"), null) => }
    property.get.options should matchPattern { case RelationPropertyOptions(false, false, true, false, false) => }
  }

  test("List to List relation") {
    val property = ModelProvider.property { _: Mock0 =>
      def list = ListField(Mock1)
    }
    
    property.get.tpe should matchPattern { case MRelation(ListEnd("Mock0"), ListEnd("Mock1"), null) => }
    property.get.options should matchPattern { case RelationPropertyOptions(false, false, true, false, false) => }
  }
  
  test("ReverseField of ObjectField") {
    val property = ModelProvider.property { _: Mock0 =>
      def reverse = ReverseField(Mock1.obj)
    }
    
    property.get.tpe should matchPattern { case MRelation(_, _, "obj") => }
    property.get.options should matchPattern { case RelationPropertyOptions(false, false, true, false, true) => }
  }
  
  test("ReverseField of ListField") {
    val property = ModelProvider.property { _: Mock0 =>
      def reverse = ReverseField(Mock1.list)
    }
    
    property.get.tpe should matchPattern { case MRelation(_, _, "list") => }
    property.get.options should matchPattern { case RelationPropertyOptions(false, false, true, false, true) => }
  }
}
