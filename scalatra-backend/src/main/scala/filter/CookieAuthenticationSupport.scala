package filter

import org.scalatra.auth.ScentryStrategy
import util.UserData
import org.scalatra.Unauthorized
import org.scalatra.auth._
import org.scalatra.ScalatraBase
import org.joda.time.DateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CookieAuthenticationStrategy(protected override val app: ScalatraBase) extends ScentryStrategy[UserData] {
 
  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[UserData] = {
    val cookie = app.cookies.get(UsernameCookie.name)
    cookie.flatMap(UsernameCookie.decode _)
  }

  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.halt(Unauthorized())
  }
}

trait CookieAuthSupport extends ScentrySupport[UserData] { self: ScalatraBase =>
  protected def fromSession = { case id => UserData(id, DateTime.now)  }
  protected def toSession   = { case UserData(id, _) => id }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]


  override protected def configureScentry = {
    scentry.unauthenticated {
      scentry.strategies("Cookie").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("Cookie", app => new CookieAuthenticationStrategy(app))
  }

  protected def cookieAuth() = {
    cookieAuthWithoutCookie
    val cookie = UsernameCookie.encode(UserData(user.username, DateTime.now))
    response.addCookie(cookie)
  }

  protected def cookieAuthWithoutCookie() = {
      scentry.authenticate("Cookie")
  }
}