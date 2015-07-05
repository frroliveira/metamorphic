package metamorphic.dsl.application

case class Application(
  name: String,
  services: List[Service]) {
  
  def service(serviceName: String): Option[Service] = {
    services.find(service => service.name.equals(serviceName))
  }
}