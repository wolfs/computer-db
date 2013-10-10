package filter

import org.scalatra.ScalatraFilter
import org.scalatra.json.NativeJsonSupport
import org.scalatra.UrlGeneratorSupport
import models.Computer
import org.json4s.DefaultFormats
import util.UserData
import org.joda.time.DateTime
import org.scalatra.NoContent
import org.scalatra.Unauthorized
import org.scalatra.Forbidden

class LoginFilter extends ScalatraFilter
  with NativeJsonSupport
  with UrlGeneratorSupport
  with CookieAuthSupport {
    override implicit def jsonFormats = DefaultFormats

    post("/api/login/:username") {
      val username = params("username")
      val password = (parsedBody \ "password").extract[String]
      if (username == "admin" && password == "1234") {
        val cookie = UsernameCookie.encode(UserData(username, DateTime.now))
        response.addCookie(cookie)
        NoContent()
      } else {
        Unauthorized()
      }
    }

    delete("/api/login/:username") {
      cookieAuthWithoutCookie
      val username = params("username")
      val userData = user
      if (userData.username == username) {
        cookies.delete(UsernameCookie.name)
      } else {
        Forbidden()
      }
    }
  }

