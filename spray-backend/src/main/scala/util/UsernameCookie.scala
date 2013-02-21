package util

import spray.http.{DateTime => SprayDateTime}
import org.joda.time.DateTime


object UsernameCookie extends AbstractUsernameCookie {
  type Cookie = spray.http.HttpCookie

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
  ): Cookie = spray.http.HttpCookie(name, content, SprayDateTime.fromIsoDateTimeString(expires.toString()), maxAge.map(_.toLong), domain, path, secure.getOrElse(false), httpOnly, extension)
}
