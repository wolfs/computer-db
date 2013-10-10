package filter

import util._
import unfiltered.filter.Plan
import unfiltered.request.{DELETE, Path, POST, HttpRequest}
import unfiltered.response._
import unfiltered.Cookie
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.scalaz.JsonScalaz._
import _root_.util.json._
import java.net.URLEncoder
import org.joda.time.DateTime
import Authentication._
import javax.servlet.http.HttpServletRequest
import _root_.util.UserData
import filter.util.JsonBody

object LoginPlan extends Plan {
  val Seg = PrefixSeg("api", "login")

  def badResultOnError[A,B](result: Result[A])(f: A => ResponseFunction[B]) = {
      result.fold(
          x => BadRequest ~> ResponseString(x.toString),
          x => f(x)
          )
  }
  
  implicit val formats = DefaultFormats  

  def intent: Plan.Intent = unfiltered.kit.GZip {
    case req@Path(Seg(idString :: Nil)) => req match {
      case POST(_) => {
        val json = JsonBody[JValue](req)

        badResultOnError(json) { json =>
          val password = (json \ "password").extract[String] 
          if (idString == "admin" && password == "1234") {
            NoContent ~> SetCookies(
              UsernameCookie.encode(UserData(idString, DateTime.now)))
          } else {
            Unauthorized
          }
        }
      }
      case DELETE(_) => req match {
        case ExtractUserData(userData) if (userData.username == idString) => NoContent ~> SetCookies(UsernameCookie.discarding)
        case _ => Unauthorized
      }
      case _ => Pass
    }
    case _ => Pass
  }
}
