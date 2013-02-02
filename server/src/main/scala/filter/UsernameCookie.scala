package filter

import _root_.util.Crypto
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.native.scalaz._
import _root_.util.json._
import unfiltered.Cookie

/**
 * @author wolfs
 */
object UsernameCookie {

  val crypto = Crypto("E27D^[_<Lpt0vjad]de;3;tx3gpRmG4ByofnahOIo9gbsMWut1w3xg[>9W")

  val isSigned = true

  val name = "LOGGED_IN_USER_COMPUTER_APP"

  def encode(username: String): Cookie = {
    val json = JObject("username" -> JString(username))
    val string = if (isSigned) {
      val toSign = json.shows
      val hash = crypto.sign(toSign)
      JObject(
        "data" -> json,
        "hash" -> JString(hash)
      ).shows
    } else
      json.shows
    Cookie(name = name, value = java.net.URLEncoder.encode(string, "UTF-8"))
  }

  def decode(data: String): String = {
    def urlDecode(data: String) = java.net.URLDecoder.decode(data, "UTF-8")

    // Do not change this unless you understand the security issues behind timing attacks.
    // This method intentionally runs in constant time if the two strings have the same length.
    // If it didn't, it would be vulnerable to a timing attack.
    def safeEquals(a: String, b: String) = {
      if (a.length != b.length) {
        false
      } else {
        var equal = 0
        for (i <- Array.range(0, a.length)) {
          equal |= a.charAt(i) ^ b.charAt(i)
        }
        equal == 0
      }
    }

    try {
      val json = urlDecode(data).asJson
      if (isSigned) {
        val data = json \ "data"
        val message = (data \ "username").convertTo[String].getOrElse("")
        if (safeEquals((json \ "hash").convertTo[String].getOrElse(""), crypto.sign(data.shows)))
          message
        else
          ""
      } else (json \ "username").convertTo[String].getOrElse("")
    } catch {
      // fail gracefully is the session cookie is corrupted
      case _: Exception => ""
    }

  }

}
