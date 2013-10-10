package services

import util.Json4sSupport
import org.json4s._
import org.json4s.native.JsonMethods._
import spray.routing._
import spray.http.{DateTime => SDateTime, _}
import StatusCodes._
import util.UsernameCookie
import util.UserData
import org.joda.time.DateTime
import util.CookieAuthentication
import org.json4s.JsonAST.JValue

trait LoginService extends Directives with Json4sSupport with CookieAuthentication {

  implicit val formats = DefaultFormats

  val loginRoute =
    path("api" / "login" / Segment) { username =>
      (post & entity(as[JValue])) { user =>
        val password = (user \ "password").extract[String]
        if (username == "admin" && password == "1234") {
          setCookie(UsernameCookie.encode(UserData(username, DateTime.now))) {
            complete(NoContent)
        }} else {
          complete(Unauthorized)
        }
      } ~
      (delete & cookieAuthDiscardingCookie) { userData =>
        authorize(userData.username == username) {
          complete(NoContent)
        }
      }
  }
}