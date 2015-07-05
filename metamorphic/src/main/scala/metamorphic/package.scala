import com.typesafe.config.{Config, ConfigException}

package object metamorphic {

  implicit class MetamorphicConfigImplicits(config: Config) {

    def getOptionString(directory: String): Option[String] = {
      try {
        Some(config.getString(directory))
      } catch {
        case _: ConfigException => None
      }
    }
    
    def getStringOrElse(directory: String, value: String): String = {
      try {
        config.getString(directory)
      } catch {
        case _: ConfigException => value
      }
    }

    def getStringOrNull(directory: String): String = {
      getStringOrElse(directory, null)
    }

    def getOptionInt(directory: String): Option[Int] = {
      try {
        Some(config.getInt(directory))
      } catch {
        case _: ConfigException => None
      }
    }

    def getIntOrElse(directory: String, value: Int): Int = {
      try {
        config.getInt(directory)
      } catch {
        case _: ConfigException => value
      }
    }
  }
}
