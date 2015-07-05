package metamorphic.generator.slick.postgres

import metamorphic.{MetamorphicException, Settings}
import metamorphic.generator.slick.SlickAsyncDriverGenerator

import scala.reflect.macros.blackbox.Context

class SlickPostgresAsyncDriverGenerator extends SlickAsyncDriverGenerator {

  def validate: Unit = {
    if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("Postgres requires database name.")
    } else if (Settings.defaultDatabase.user == null) {
      throw new MetamorphicException("Postgres requires database user.")
    } else if (Settings.defaultDatabase.password == null) {
      throw new MetamorphicException("Postgres requires database password.")
    } else if (Settings.defaultDatabase.host == null) {
      throw new MetamorphicException("Postgres requires database host.")
    } else if (Settings.defaultDatabase.port == -1) {
      throw new MetamorphicException("Postgres requires database port.")
    }
  }

  def driver(implicit c: Context): c.Tree = {
    import c.universe._

    q"slick.driver.PostgresDriver"
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
        password = Settings.defaultDatabase.password,
        executor = AsyncExecutor(
          Settings.defaultDatabase.name, 
          numThreads = Settings.defaultDatabase.numThreads, 
          queueSize = Settings.defaultDatabase.queueSize
        )
      )
    """
  }
}
