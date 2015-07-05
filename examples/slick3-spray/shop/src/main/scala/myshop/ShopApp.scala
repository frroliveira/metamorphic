package myshop

import metamorphic.dsl._

@app
object ShopApp {
 
  @entity class Address {
    def destinatary = StringField()
    def street = StringField()
    def zipCode = StringField()
    def city = StringField()
    def country = StringField()
  }
  
  @entity class Customer {
    def email = StringField()
    def name = StringField()
    def birthdate = DateField(Option)
    def shipTo = ObjectField(Address, R.Object)
    def invoiceTo = ObjectField(Address, R.Object)
  }
  
  @entity class Category {
    def name = StringField()
    def description = StringField()
  }
  
  @entity class Product {
    def name = StringField()
    def description = StringField()
    def cost = DoubleField()
    def isAvailable = BooleanField()
    def brand = StringField()
    def category = ObjectField(Category)
  }
  
  @entity class Order {
    def reference = StringField()
    def datetime = DateTimeField()
    def shippingCost = DoubleField(Option)
    def state = IntegerField()
    def products = ListField(Product)
    def customer = ObjectField(Customer)
  }
  
  @entity class Shop {
    def opens = DateTimeField()
    def closes = DateTimeField()
    def products = ListField(Product)
  }

  class CustomerService extends EntityService[Customer] {
    val operations = List(Get, Create, Replace)
  }

  class CategoryService extends EntityService[Category] {
    val operations = List(GetAll, Create, Delete)
  }
  
  class OrderService extends EntityService[Order] {
    val operations = List(Get, Create, Replace)
    
    def create(order: Order) = {
      if (order.products.length == 0)
        Future(Response("Order must have at least one product", BadRequest))
      else
        super.create(order)
    }
  }
  
  class ProductService extends EntityService[Product] {
  
    def getAll = {
      repository.getAll.map(products => {
        val result = products.filter(product => product.isAvailable)
        Response(result, Ok)
      })
    }
  }
}
