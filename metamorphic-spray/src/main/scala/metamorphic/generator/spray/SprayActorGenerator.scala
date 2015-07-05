package metamorphic.generator.spray

import metamorphic.dsl.application._
import metamorphic.dsl.generator._
import metamorphic.dsl.model._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

object SprayActorGenerator {

  def generate(application: Application)(implicit c: Context): c.Tree = {
    import c.universe._
        
    val depends = "Actor".toType :: application.services.map(service => service.name.toType)
    val route = UtilGenerator.recursiveMethod(application.services, new ServiceRecursiveApplication)
    
    q"""
      class ${application.name.actor.toType} extends ..$depends {
        def actorRefFactory = context
        def receive = runRoute($route)
      }
    """
  }

  class ServiceRecursiveApplication extends RecursiveApplication[Service] {
    
    def method: String = "$tilde"
    
    def nilError: String = "Unable to generate actor. No services defined."
    
    def apply(service: Service)(implicit c: Context): c.Tree = {
      import c.universe._
      
      Ident(service.name.route.obj.toTerm)
    }
  }
}

