package metamorphic

import metamorphic.db._

import com.typesafe.config.ConfigFactory

object Settings {

  val config = ConfigFactory.load(getClass.getClassLoader)
    .getConfig("metamorphic")
  
  val databases = config.getConfig("databases")
  
  def host: String =
    config.getString("host")

  def port: Int =
    config.getInt("port")
    
  def debug: Boolean =
    config.getBoolean("debug")
  
  def macroDebug: Boolean =
    config.getBoolean("macroDebug")
    
  def repositoryGenerator: Option[String] =
    config.getOptionString("repositories.default.generator")

  def serviceGenerator: Option[String] =
    config.getOptionString("services.default.generator")

  def defaultDatabase: DatabaseConfig = {
    database("default")
  }

  def database(path: String): DatabaseConfig = {
    val system = databases.getStringOrNull(path + ".system")
    val name = databases.getStringOrNull(path + ".name")

    if (name == null) {
      throw new MetamorphicException(s"Database name not specified in '$path'.")
    } else {

      val user = databases.getStringOrNull(path + ".user")
      val password = databases.getStringOrNull(path + ".password")
      val host = databases.getStringOrNull(path + ".host")
      val port = databases.getIntOrElse(path + ".port", -1)
      val numThreads = databases.getIntOrElse(path + ".numThreads", 10)
      val queueSize = databases.getIntOrElse(path + ".queueSize", 1000)

      DatabaseConfig(system, name, user, password, host, port, numThreads, queueSize)
    }
  }
}
