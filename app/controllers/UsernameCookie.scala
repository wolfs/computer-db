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

  protected def deserialize(data: Map[String, String]): Option[Username] = Some(Username(data("username")))

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

}
