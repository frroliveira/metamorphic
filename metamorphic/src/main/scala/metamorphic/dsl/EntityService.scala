package metamorphic.dsl

trait EntityService[T] {
  
  def repository: Repository[T] = ???
  
  def getAll: Response = ???
  def get(id: Int): Response = ???
  def create(instance: T): Response = ???
  def replace(instance: T): Response = ???
  def delete(id: Int): Response = ???
}

trait Repository[T] {

  def getAll: List[T]
  def get(id: Int): Option[T]
  def create(instance: T): T
  def replace(instance: T): Option[T]
  def delete(id: Int): Boolean
}
