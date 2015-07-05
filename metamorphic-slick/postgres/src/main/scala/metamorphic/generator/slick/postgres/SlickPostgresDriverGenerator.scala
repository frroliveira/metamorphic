package metamorphic.generator.slick.postgres

import metamorphic.{MetamorphicException, Settings}
import metamorphic.generator.slick.SlickDriverGenerator

import scala.reflect.macros.blackbox.Context

class SlickPostgresDriverGenerator extends SlickDriverGenerator {

  def validate: Unit = {
    if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("Postgres requires database name.")
    } else if (Settings.defaultDatabase.user == null) {
      throw new MetamorphicException("Postgres requires database user.")
    } else if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("Postgres requires database password.")
    } else if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("Postgres requires database host.")
    } else if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("Postgres requires database port.")
    }
  }

  def driver(implicit c: Context): c.Tree = {
    import c.universe._

    q"scala.slick.driver.PostgresDriver"
  }
  
  def database(implicit c: Context): c.Tree = {
    import c.universe._

    q"""
      Database.forURL("jdbc:postgresql://" + 
        Settings.defaultDatabase.host + ":" +
        Settings.defaultDatabase.port + "/" +
        Settings.defaultDatabase.name,
        driver = "org.postgresql.Driver",
        user = Settings.defaultDatabase.user,
        password = Settings.defaultDatabase.password
      )
    """
  }
}
