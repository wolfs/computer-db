package filter

import util.AbstractUsernameCookie
import org.joda.time.DateTime

object UsernameCookie extends AbstractUsernameCookie {
  type Cookie = org.scalatra.Cookie

  override def Cookie(
    name: String,
    content: String,
    expires: Option[DateTime] = None,
    maxAge: Option[Int] = None,
    domain: Option[String] = None,
    path: Option[String] = None,
    secure: Option[Boolean] = None,
    httpOnly: Boolean = false,
    extension: Option[String] = None
  ): Cookie = {
    val options = org.scalatra.CookieOptions(
        maxAge = maxAge.getOrElse(-1),
        domain = domain.getOrElse(""),
        path = path.getOrElse(""),
        secure = secure.getOrElse(false),
        httpOnly = httpOnly)
    org.scalatra.Cookie(name, content)(options)
  }

}