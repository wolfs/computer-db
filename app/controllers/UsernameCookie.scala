package controllers

import play.api.mvc.{Cookie, CookieBaker}
import play.api.libs.Crypto
import play.api.libs.json.{JsString, JsObject, Json}


case class Username(username: String) {
}
/**
 * @author wolfs
 */
object UsernameCookie extends CookieBaker[Option[Username]]{
  def COOKIE_NAME: String = "LOGGED_IN_USER_COMPUTER_APP";

  override def isSigned: Boolean = true


  override def httpOnly: Boolean = false

  def emptyCookie: Option[Username] = None;

  protected def deserialize(data: Map[String, String]): Option[Username] = if (data.isEmpty) emptyCookie else Some(Username(data("username")))

  protected def serialize(cookie: Option[Username]): Map[String, String] = cookie match {
    case None => Map[String,String]()
    case Some(Username(username)) => Map("username" -> username)
  }

  override def encode(data: Map[String, String]): String = {
    val json = Json.toJson(data)
    val string = if (isSigned) {
      val toSign = json.toString()
      val hash = Crypto.sign(toSign)
      JsObject(
        Seq("data" -> json,
        "hash" -> JsString(hash))
      ).toString()
    } else
      json.toString()
    java.net.URLEncoder.encode(string, "UTF-8")
  }

  override def decode(data: String): Map[String, String] = {
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
      val json = Json.parse(urlDecode(data))
      if (isSigned) {
        val data = json \ "data"
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
