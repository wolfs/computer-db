package filter

import org.joda.time.DateTime
import _root_.util.AbstractUsernameCookie

object UsernameCookie extends AbstractUsernameCookie {
  type Cookie = unfiltered.Cookie
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
  ): Cookie = unfiltered.Cookie(name, content, domain, path, maxAge, secure, httpOnly)
}
