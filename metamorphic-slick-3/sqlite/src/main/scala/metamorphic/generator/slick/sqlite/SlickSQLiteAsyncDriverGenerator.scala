package metamorphic.generator.slick.sqlite

import metamorphic.{Settings, MetamorphicException}
import metamorphic.generator.slick.SlickAsyncDriverGenerator

import scala.reflect.macros.blackbox.Context

class SlickSQLiteAsyncDriverGenerator extends SlickAsyncDriverGenerator {

  def validate: Unit = {
    if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("SQLite requires database name.")
    }
  }

  def driver(implicit c: Context): c.Tree = {
    import c.universe._

    q"slick.driver.SQLiteDriver"
  }
  
  def database(implicit c: Context): c.Tree = {
    import c.universe._

    q"""Database.forURL("jdbc:sqlite:" + Settings.defaultDatabase.name, driver = "org.sqlite.JDBC")"""
  }
}
