package metamorphic.generator.spray

import metamorphic.dsl.application._
import metamorphic.dsl.generator._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

object SprayRouteGenerator {

  def generate(service: Service)(implicit c: Context): c.Tree = {
    import c.universe._
    
    val routes = UtilGenerator.recursiveMethod(service.operations, new OperationRecursiveApplication)

    q"""
      trait ${service.name.toType} extends HttpService with ApplicationJson {
        ..${service.dependencies.trees}
        import scala.language.postfixOps
        import scala.util._
        import metamorphic.Settings
          
        val ${service.name.route.obj.toTerm} = $routes
      }
    """
  }
  
  class OperationRecursiveApplication extends RecursiveApplication[Operation] {
    
    def method: String = "$tilde"
    
    def nilError: String = "No operations defined for service."
    
    def apply(operation: Operation)(implicit c: Context): c.Tree = {
      import c.universe._
      
      def path: Tree = {
        operation.path.parameter match {
          case Some(parameter) =>
            q"path(${operation.path.base} / IntNumber /) { $pathContent }"
          case None =>
            q"path(${operation.path.base} /) { $pathContent }"
        }
      }
      
      def pathContent: Tree = {
        operation.path.parameter match {
          case Some(parameter) =>
            q"""(${parameter.toTerm}: ${tq""}) => $method"""
          case None => method
        }
      }
      
      def method: Tree = {
        operation.method match {
          case Get() => q"get { dynamic { $methodContent } }"
          case Post() => q"post { dynamic { $methodContent } }"
          case Put() => q"put { dynamic { $methodContent } }"
          case Delete() => q"delete { dynamic { $methodContent } }"
        }
      }
      
      def methodContent: Tree = {
        operation.content match {
          case Some(content) => q"entity(as[${content.tpe.toType}]) { $code }"
          case None => code
        }
      }
      
      def code: Tree = {
        operation.content match {
          case Some(content) => q"(${content.name.toTerm}: ${tq""}) => { ..$transformBody }"
          case None => q"{ ..$transformBody }"
        }
      }
      
      def transformBody: List[c.Tree] = {
        val transformed = operation.body.trees.map(tree => 
          UtilGenerator.transformByPattern(c)(tree, {
            case q"Response($content, $status)" => {
              status.toString match {
                case "Ok" => q"complete($content)"
                case "Created" => q"complete(StatusCodes.Created, $content)"
                case "BadRequest" => q"complete(StatusCodes.BadRequest, $content)"
                case "NoContent" => q"complete(StatusCodes.NoContent, $content)"
              }
            }
          })
        )

        if (!operation.isAsync) {
          transformed
        } else {
          q"""
            val future = {
              ..$transformed
            }

            onComplete(future) {
              case Success(route) => route
              case Failure(ex) => {
                if (Settings.debug) println(ex)
                complete(StatusCodes.InternalServerError, ex)
              }
            }
          """.children
        }
      }

      path
    }
  }
}
