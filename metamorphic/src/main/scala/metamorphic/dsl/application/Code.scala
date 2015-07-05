package metamorphic.dsl.application

import scala.reflect.macros.blackbox.Context

case class Code(value: Any) {

  def trees(implicit c: Context): List[c.Tree] = {
    value match {
      case trees: List[c.Tree] => trees
    }
  }
  
  def merge(code: Code)(implicit c: Context): Code = {
    Code(trees ++ code.trees)
  }
}