package metamorphic.generator.slick.h2

import metamorphic.{MetamorphicException, Settings}
import metamorphic.generator.slick.SlickDriverGenerator

import scala.reflect.macros.blackbox.Context

class SlickH2DriverGenerator extends SlickDriverGenerator {

  def validate: Unit = {
    if (Settings.defaultDatabase.name == null) {
      throw new MetamorphicException("H2 requires database name.")
    }
  }

  def driver(implicit c: Context): c.Tree = {
    import c.universe._

    q"scala.slick.driver.H2Driver"
  }
  
  def database(implicit c: Context): c.Tree = {
    import c.universe._

    q"""Database.forURL("jdbc:h2:mem:" + Settings.defaultDatabase.name + ";DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")"""
  }
}
