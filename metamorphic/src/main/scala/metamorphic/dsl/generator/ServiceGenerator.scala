package metamorphic.dsl.generator

import metamorphic.dsl.application.Application

import scala.reflect.macros.blackbox.Context

trait ServiceGenerator {

  def name: String
  def generate(application: Application)(implicit c: Context): List[c.Tree]
}
