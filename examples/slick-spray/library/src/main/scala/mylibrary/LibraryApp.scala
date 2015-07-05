package mylibrary

import metamorphic.dsl._

@app
object LibraryApp {

  @entity class Address {
    def organization = StringField()
    val author = ReverseField(Author.address)
  }
  
  @entity class Author {
    def name = StringField()
    def address = ObjectField(Address, Composition)
    val books = ReverseField(Book.author)
  }

  @entity class Book {
    def name = StringField()
    def copies = IntegerField()
    def price = DoubleField()
    def author = ObjectField(Author)
    def library = ObjectField(Library)
    def categories = ListField(Category)
  }
  
  @entity class Category {
    def name = StringField()
    def description = StringField()
    def library = ObjectField(Library)
    val books = ReverseField(Book.categories)
  }

  @entity class Library {
    def name = StringField()
    val books = ReverseField(Book.library)
    val categories = ReverseField(Category.library)
  }
  
  val operations = List(Create, Get, Replace)
  
  class BookService extends EntityService[Book] {
        
    def getAll = {
      val result = repository.getAll.filter(book => book.price < 10.0)
      Response(result, Ok)
    }

    def create(book: Book) = {
      if (book.name.length > 10)
        Response("Invalid 'name'. No longer than 10 characters.", BadRequest)
      else 
        super.create(book)
    }

    def get(id: Int) = {
      super.get(id)
    }
    
    def replace(book: Book) = {
      super.replace(book)
    }
    
    def delete(id: Int) = {
      super.delete(id)
    }
  }

  class LibraryService extends EntityService[Library] {
    
    val operations = List(Get, Replace, Delete)
  }
}
