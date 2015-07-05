package metamorphic.dsl.util

object Instantiator {

  def instance[T](className: String): T = {
    val clasz = Class.forName(className)
    val constructor = clasz.getConstructor()
    constructor.newInstance().asInstanceOf[T]
  }
  
  def instance[T](className: Option[String]): Option[T] = {
    className match {
      case Some(className) => Some(instance[T](className))
      case None => None
    }
  }  
}
