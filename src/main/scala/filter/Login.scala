package filter

import unfiltered.filter.Plan
import util.PrefixSeg
import unfiltered.request.{DELETE, Path, POST}
import unfiltered.response.{NoContent, SetCookies, ToCookies}
import unfiltered.Cookie
import spray.json.DefaultJsonProtocol._
import spray.json.{JsString, JsObject}

/**
 * @author wolfs
 */
object Login extends Plan {
  val Seg = PrefixSeg("api", "login")

  def intent: Plan.Intent = {
    case req@Path(Seg(idString :: Nil)) => req match {
      case POST(_) => NoContent ~> SetCookies(Cookie("LOGGED_IN_USER_COMPUTER_APP", java.net.URLEncoder.encode(JsObject("data" -> JsObject("username" -> JsString("admin"))).compactPrint, "UTF-8")))
      case DELETE(_) => NoContent ~> SetCookies.discarding("LOGGED_IN_USER_COMPUTER_APP")
    }
  }
}
