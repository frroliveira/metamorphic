package metamorphic.dsl.util

import scala.reflect.macros.blackbox.Context

object StringImplicits {
  
  implicit class StringImplicitsImpl(s: String) {
    
    def id: String = s.lowerFirst + "Id"
    def obj: String = s.lowerFirst
    def repository: String = s + "Repository"
    def service: String = s + "Service"

    def plural: String = {
      s.takeRight(1) match {
        case "y" => s.dropRight(1) + "ies"
        case "s" => s + "es"
        case _   => s + "s"
      }
    }
    
    def lowerFirst: String = {
      s(0).toLower + s.substring(1, s.length)
    }
    
    def upperFirst: String = {
      s(0).toUpper + s.substring(1, s.length)
    }
    
    def toType(implicit c: Context): c.universe.TypeName = {
      c.universe.TypeName(s)
    }
    
    def toTerm(implicit c: Context): c.universe.TermName = {
      c.universe.TermName(s)
    }
  }
}
