package metamorphic.generator.spray

import metamorphic.Settings
import metamorphic.dsl.application._
import metamorphic.dsl.generator._
import metamorphic.dsl.util.StringImplicits._

import scala.reflect.macros.blackbox.Context

class SprayServiceGenerator extends ServiceGenerator {
  
  def name: String = "spray"
  
  def generate(application: Application)(implicit c: Context): List[c.Tree] = {    
    import c.universe._
  
    val routes: List[Tree] = application.services
      .map(service => SprayRouteGenerator.generate(service))
    val actor: Tree = SprayActorGenerator.generate(application)
    
    q"""
      import akka.actor.Actor
      import spray.routing._
      import spray.http._
      
      $actor

      import spray.httpx.Json4sSupport
      import org.json4s.DefaultFormats
      
      trait ApplicationJson extends Json4sSupport {
        import java.util.Date
        import java.text.{ParseException, SimpleDateFormat}
        import org.json4s.{CustomSerializer, MappingException}
        import org.json4s.JsonAST.{JString, JNull}
        import org.json4s.ext.DateTimeSerializer
          
        private val simpleFormat = new SimpleDateFormat("yyyy-MM-dd")
        
        private def parse(s: String): Option[Date] = {
          try {
            Some(simpleFormat.parse(s))
          } catch {
            case e: ParseException => None
          }
        }
        
        val LocalDateSerializer = new CustomSerializer[LocalDate](
          format => ({
            case JString(s) => {
              val time = parse(s).map(_.getTime).getOrElse(throw new MappingException("Invalid date format " + s))
              new org.joda.time.DateTime(time).toLocalDate()          
            }
            case JNull => null
          }, {
            case d: LocalDate => JString(simpleFormat.format(d.toDate))
          })
        )

        val json4sFormats = DefaultFormats + DateTimeSerializer + LocalDateSerializer
      }

      ..$routes
    
      def run = {
        import akka.actor.{ActorSystem, Props}
        import akka.io.IO
        import spray.can.Http
        import akka.pattern.ask
        import akka.util.Timeout
        import scala.concurrent.duration._
        import metamorphic._
        
        implicit val system = ActorSystem("spray-can")
        implicit val timeout = Timeout(15.seconds)
        
        val service = system.actorOf(Props[${application.name.actor.toType}], ${application.name})
        IO(Http) ? Http.Bind(service, interface = Settings.host, port = Settings.port)
      }
      run
    """.children
  }
}
