package metamorphic.dsl.generator

import metamorphic.dsl.model._

import scala.reflect.macros.blackbox.Context

trait RepositoryGenerator {

  def name: String
  def isAsync: Boolean
  def generate(model: Model)(implicit c: Context): List[c.Tree]
}
