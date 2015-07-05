package metamorphic.generator.slick

import metamorphic._

import com.typesafe.config.ConfigFactory

object SlickSettings {
  
  val config = ConfigFactory.load(getClass.getClassLoader)
    .getConfig("metamorphic.repositories")

  def driverGenerator: Option[String] =
    config.getOptionString("slick.driver.generator")    
}
