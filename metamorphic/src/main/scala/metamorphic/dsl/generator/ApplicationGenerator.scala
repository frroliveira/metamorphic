package metamorphic.dsl.generator

import metamorphic.dsl.application._
import metamorphic.dsl.model._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

object ApplicationGenerator {
  
  def generate(model: Model, application: Application)(implicit c: Context): c.Expr[Any] = {    
    import c.universe._
    
    val repositoryGen = Dependencies.repositoryGenerator.get
    val serviceGen = Dependencies.serviceGenerator.get

    val caseClasses: List[Tree] = UtilGenerator.modelCaseClasses(model)
    val repositories: List[Tree] = repositoryGen.generate(model)
    val services: List[Tree] = serviceGen.generate(application)

    c.Expr(q"""
      object ${application.name.toTerm} extends App {
        import org.joda.time._
        import scala.concurrent.Future
        import scala.concurrent.ExecutionContext.Implicits.global
        import metamorphic.Settings
        
        ..$caseClasses
        
        object ${repositoryGen.name.repository.plural.toTerm} {
          ..$repositories
        }
        
        ..$services
      }
    """)
  }
}
