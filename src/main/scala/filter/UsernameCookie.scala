package filter

import _root_.util.Crypto
import spray.json.DefaultJsonProtocol._
import spray.json.JsObject
import spray.json._

/**
 * @author wolfs
 */
class UsernameCookie {

  val crypto = Crypto("E27D^[_<Lpt0vjad]de;3;tx3gpRmG4ByofnahOIo9gbsMWut1w3xg[>9W")

  val isSigned = true

  def encode(username: String): String = {
    val json = JsObject("username" -> JsString(username))
    val string = if (isSigned) {
      val toSign = json.compactPrint
      val hash = crypto.sign(toSign)
      JsObject(
        "data" -> json,
        "hash" -> JsString(hash)
      ).compactPrint
    } else
      json.compactPrint
    java.net.URLEncoder.encode(string, "UTF-8")
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
          equal |= a(i) ^ b(i)
        }
        equal == 0
      }
    }

    try {
      val json = urlDecode(data).asJson
      if (isSigned) {
        val data = json \\ "data"
        val message = Json.fromJson[Map[String, String]](data).getOrElse(Map.empty)
        if (safeEquals(Json.fromJson[String](json \ "hash").getOrElse(""), Crypto.sign(data.toString())))
          message
        else
          Map.empty[String, String]
      } else Json.fromJson[Map[String, String]](json).getOrElse(Map.empty)
    } catch {
      // fail gracefully is the session cookie is corrupted
      case _: Exception => Map.empty[String, String]
    }

  }

}
