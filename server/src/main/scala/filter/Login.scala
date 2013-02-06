package filter

import unfiltered.filter.Plan
import util.PrefixSeg
import unfiltered.request.{DELETE, Path, POST}
import unfiltered.response.{NoContent, SetCookies, ToCookies}
import unfiltered.Cookie
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.native.scalaz._
import _root_.util.json._
import java.net.URLEncoder
import org.joda.time.DateTime
import Authentication._
import unfiltered.response.Pass
import javax.servlet.http.HttpServletRequest
import unfiltered.request.HttpRequest
import unfiltered.response.ResponseString
import unfiltered.response.Unauthorized

object Login extends Plan {
  val Seg = PrefixSeg("api", "login")

  def intent: Plan.Intent = {
    case req@Path(Seg(idString :: Nil)) => req match {
      case POST(_) => NoContent ~> SetCookies(
          UsernameCookie.encode(UserData(idString, DateTime.now)))
      case DELETE(_) => req match {
        case ExtractUserData(userData) if (userData.username == idString) => NoContent ~> SetCookies(UsernameCookie.discarding)
        case _ => Unauthorized
      }
      case _ => Pass
    }
    case _ => Pass
  }
}
