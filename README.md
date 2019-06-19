<img src="http://frroliveira.github.io/assets/metamorphic.png" width="300px">

Metamorphic is a Scala framework that enables generation of model-driven REST services through the use of a DSL. These services implement CRUD operations on model entities, that can be one of the following: `GetAll`, `Create`, `Get`, `Replace`, and `Delete`. Services can be **synchronous** or **asynchronous**.

The framework's core is dependent of two projects:
* **Repository** generator - can generate a `Repository[T]` for storage of an entity `T`. There is a *synchronous* implementation with [Slick 2.1](metamorphic-slick) and an *asynchronous* with [Slick 3.0](metamorphic-slick-3).
* **Service** generator - can generate code that offers the services. There is an implementation with [Spray](metamorphic-spray).

For syntax simplicity should be used *synchronous* implementations, while for performance should be used *asynchronous*, as shown by [Benchmarks](validation/benchmarks).

# Getting Started

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/58b9443bd3f74627acdb770f5b37c8a1)](https://www.codacy.com/app/filipeoliveirr/metamorphic?utm_source=github.com&utm_medium=referral&utm_content=frroliveira/metamorphic&utm_campaign=badger)

The framework will be deployed to Maven Central Repository in the future. Meanwhile you can clone this repository: 

```
git clone https://github.com/frroliveira/metamorphic.git
```

And start with:
* Empty example: [slick-spray/empty](examples/slick-spray/empty) and [slick3-spray/empty](examples/slick3-spray/empty).
* Complete example: [slick-spray/library](examples/slick-spray/library) and [slick3-spray/shop](examples/slick3-spray/shop).

You may have installed:

* Scala 2.11.x
* SBT 0.13.x

# Example

Import the DSL for use:

```scala
import metamorphic.dsl._
```

Define entities and services:

```scala
@app
object PersonApp {

  @entity class Person {
    def fullname = StringField()
    def birthdate = DateField()
  }
  
  class PersonService extends EntityService[Person] {
    val operations = List(Create, GetAll)
  }
}
```

Set your configurations in `conf/application.conf`:

```
metamorphic {
  port = 9000
  databases.default.name = "file.db"
}
```

# Documentation

The framework is based in four key components.

* [Entities](#entities)
* [Operations](#operations)
* [Services](#services)
* [Repositories](#repositories)
* [Configurations](#configurations)

## Entities

Use `@entity` to specify a model entity.

```scala
@entity class Person {
  def fullname = StringField()
  def birthdate = DateField()
}
```

These are mapped to case classes with an automatic property `id` of type `Option[Int]`. The fields are mapped according with the table:

<table>
  <tr>
    <th>Type</th>
    <th>Field</th>
    <th>Scala type</th>
    <th>Example</th>
  </tr>
  <tr>
    <td rowspan="6">Primitive</td>
    <td>StringField</td>
    <td>String</td>
    <td>"John Mayer"</td>
  </tr>
  <tr>
    <td>DoubleField</td>
    <td>Double</td>
    <td>10.0</td>
  </tr>
  <tr>
    <td>IntegerField</td>
    <td>Int</td>
    <td>10</td>
  </tr>
  <tr>
    <td>BooleanField</td>
    <td>Boolean</td>
    <td>true</td>
  </tr>
  <tr>
    <td>DateField</td>
    <td>org.joda.time.LocalDate</td>
    <td>"2015-07-21"</td>
  </tr>
  <tr>
    <td>DateTimeField</td>
    <td>org.joda.time.DateTime</td>
    <td>"2015-07-21T20:01:23Z"</td>
  </tr>
  <tr>
    <td rowspan="2">Relation between entities</td>
    <td>ObjectField(Entity)</td>
    <td>Int</td>
    <td>2</td>
  </tr>
  <tr>
    <td>ListField(Entity)</td>
    <td>List[Int]</td>
    <td>[3, 4, 7]</td>
  </tr>
</table>

This means that `Person` is mapped to:

```scala
case class (
  id: Option[Int],
  fullname: String,
  birthdate: org.joda.time.LocalDate
)
```

## Operations

By default, all operations are implemented for each entity. These operations can be redefined using the `operations` variable in the `@app` scope. This variable when used inside a service scope only affects that service.

```scala
val operations = List(Create, GetAll, Replace)
```

Considering `basePath` as the plural of an entity, its operations are implemented as follows:

Operation | HTTP method | Path         | Success code     | Error codes
--------- | ----------- | ------------ | ---------------- | -----------
Create    | POST        | basePath/    | 201 (Created)    | 400 (Bad Request)
GetAll    | GET         | basePath/    | 200 (Ok)         |
Get       | GET         | basePath/:id | 200 (Ok)         | 404 (Not Found)
Replace   | PUT         | basePath/:id | 200 (Ok)         | 400 (Bad Request), 404 (Not Found)
Delete    | DELETE      | basePath/:id | 204 (No Content) | 404 (Not Found)

This means that *Create* of `Person` would behave as follows:

```bash
$ curl -X POST -d '{ "fullname": "John Mayer", "birthdate": "1982-10-18" }' http://<host>:<port>/persons
```

```bash
{
  "id": 1,
  "fullname": "John Mayer",
  "birthdate": "1982-10-18"
}
```

## Services

Services can be customized extending `EntityService[T]`, where `T` is an entity. Using repository generators, services instantiate a `repository: Repository[T]` that can be used by customizations.

Services with **synchronous** operations may implement:

```scala
trait EntityService[T] {
  def getAll: Response
  def get(id: Int): Response
  def create(instance: T): Response
  def replace(instance: T): Response
  def delete(id: Int): Response
}
```

While services with **asynchronous** operations may implement:

```scala
trait EntityService[T] {
  def getAll: Future[Response]
  def get(id: Int): Future[Response]
  def create(instance: T): Future[Response]
  def replace(instance: T): Future[Response]
  def delete(id: Int): Future[Response]
}
```

A `Response` contains a content and a `StatusCode` as defined [here](metamorphic/src/main/scala/metamorphic/dsl/StatusCode.scala).

```scala
case class Response(content: Any, code: StatusCode)
```

#### Synchronous Example

```scala
class PersonService extends EntityService[Person] {
  def getAll = {
    val result = repository.getAll.filter(person => person.birthdate.getYear() > 2000)
    Response(result, Ok)
  }

  def create(person: Person) = {
    if (person.fullname.length < 5)
      Response("Name is too short.", BadRequest)
    else 
      super.create(person)
  }
}
```

## Repositories

**Synchronous** repository operations implement:

```scala
trait Repository[T] {
  def getAll: List[T]
  def get(id: Int): Option[T]
  def create(instance: T): T
  def replace(instance: T): Option[T]
  def delete(id: Int): Boolean
}
```

While **asynchronous** repository operations implement:

```scala
trait Repository[T] {
  def getAll: Future[List[T]]
  def get(id: Int): Future[Option[T]]
  def create(instance: T): Future[T]
  def replace(instance: T): Future[Option[T]]
  def delete(id: Int): Future[Boolean]
}
```

## Configurations

Configurations are specified in `conf/application.conf`.

```
metamorphic {
  port = 9000
  databases.default.name = "file.db"
}
```

Application level configurations are preceded by `metamorphic` and can be:

Key        | Type    | Default
---------- | ------- | -----------
host       | String  | "localhost"
port       | Int     | 8080
debug      | Boolean | false
macroDebug | Boolean | false

Database configurations are preceded by `metamorphic.databases.default` and can be:

Key        | Type   | Example
---------- | ------ | -----------
name       | String | "persondb"
user       | String | "user"
password   | String | "password"
host       | String | "localhost"
port       | Int    | 5432
numThreads | Int    | 10
queueSize  | Int    | 10000

