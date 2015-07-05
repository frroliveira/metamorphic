package metamorphic.generator.slick

import scala.reflect.macros.blackbox.Context

trait SlickDriverGenerator {

  def validate: Unit
  def driver(implicit c: Context): c.Tree
  def database(implicit c: Context): c.Tree
}