package metamorphic.generator.slick.sqlite

import metamorphic.{Settings, MetamorphicException}
import metamorphic.generator.slick.SlickDriverGenerator

import scala.reflect.macros.blackbox.Context

class SlickSQLiteDriverGenerator extends SlickDriverGenerator {

  def validate: Unit = {
    if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("SQLite requires database name.")
    }
  }

  def driver(implicit c: Context): c.Tree = {
    import c.universe._

    q"scala.slick.driver.SQLiteDriver"
  }
  
  def database(implicit c: Context): c.Tree = {
    import c.universe._

    q"""Database.forURL("jdbc:sqlite:" + Settings.defaultDatabase.name, driver = "org.sqlite.JDBC")"""
  }
}
