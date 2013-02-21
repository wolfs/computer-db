package filter

import unfiltered.Cycle
import unfiltered.Cookie
import unfiltered.response._
import unfiltered.response.ResponseFunction
import unfiltered.request._
import org.joda.time.DateTime
import unfiltered.filter.Plan
import javax.servlet.http.HttpServletResponse
import language.reflectiveCalls
import _root_.util.UserData

object Authentication {
  type Intent = Cycle.Intent[Any,Any]
  type Response = Any
  type AuthorizedIntent[-A,-B] = PartialFunction[HttpRequest[A], UserData => ResponseFunction[B]]

  def defaultFail = Unauthorized
  def auth(
    intent: Intent, onFail: ResponseFunction[Response] = defaultFail, discardCookie: Boolean = false) = {
    intent.fold(
      { _ => Pass },
      {
        case (ExtractUserData(userData), rf) => {
          rf ~> (if (discardCookie)
            SetCookies.discarding(UsernameCookie.name) else
              SetCookies(
                  UsernameCookie.encode(userData.copy(expiryDate = DateTime.now.plusMinutes(5)))
              )
          )
        }
        case _ => onFail
      }
    )
  }
}

object ExtractUserData {
  def unapply[T](r: HttpRequest[T]) = r match {
      case Cookies(cookies) => {
        val cookie = cookies(UsernameCookie.name)
        cookie flatMap { cookie => UsernameCookie.decode(cookie.value) }
      }
  }
  def apply[T](r: HttpRequest[T]) = unapply(r)
}