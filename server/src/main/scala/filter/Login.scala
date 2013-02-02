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

/**
 * @author wolfs
 */
object Login extends Plan {
  val Seg = PrefixSeg("api", "login")

  def intent: Plan.Intent = {
    case req@Path(Seg(idString :: Nil)) => req match {
      case POST(_) => NoContent ~> SetCookies(
          UsernameCookie.encode(idString))
      case DELETE(_) => NoContent ~> SetCookies.discarding(UsernameCookie.name)
    }
  }
}
