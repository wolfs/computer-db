package util

import spray.routing.authentication._
import spray.routing.RequestContext
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing.Directive
import shapeless._
import spray.routing._
import spray.routing.directives.CookieDirectives._
import spray.routing.directives.MiscDirectives._
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.RespondWithDirectives._
import spray.routing.authentication._
import spray.routing.directives.SecurityDirectives
import org.joda.time.DateTime
import spray.http.HttpCookie
import spray.http.HttpHeader

trait CookieAuthentication extends SecurityDirectives {
  case class `Set-Cookie`(cookie: HttpCookie) extends HttpHeader {
    def name = "Set-Cookie"
    def lowercaseName = "set-cookie"
    def value = cookie.value.replaceFirst("=\"([^\"]+)\"", "=$1")
  }

  def mySetCookie(cookie: HttpCookie): Directive0 = respondWithHeader(`Set-Cookie`(cookie))

  def cookieAuthBase: Directive[UserData :: HNil] = {
    cancelAllRejections(ofType[MissingCookieRejection]).hflatMap {
      case HNil =>
        optionalCookie(UsernameCookie.name).flatMap { cookieOption =>
          authenticate(
            Future.successful(
              (for {
                cookie <- cookieOption
                userData <- UsernameCookie.decode(cookie.content)
              } yield {
                Right(userData)
              }) getOrElse Left(AuthenticationFailedRejection("hier"))
            )
          )
        }
    }
  }


  def cookieAuth: Directive[UserData :: HNil] = {
    cookieAuthBase flatMap { userData =>
      mySetCookie(UsernameCookie.encode(userData.copy(expiryDate = DateTime.now))) & provide(userData)
    }
  }
  def cookieAuthDiscardingCookie: Directive[UserData :: HNil] = {
      cookieAuthBase flatMap { userData =>
        deleteCookie(UsernameCookie.discarding) & provide(userData)
      }
  }

}