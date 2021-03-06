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
import org.joda.time.DateTime
import org.json4s.ext.DateTimeSerializer._
import org.joda.time.Minutes

case class UserData(username: String, expiryDate: DateTime)

object UsernameCookie {
  implicit val dateFormat: JSON[DateTime] = new JSON[DateTime] {
    def write(value: DateTime): JValue = {
      JString(value.toString())
    }

    def read(json: JValue): Result[DateTime] = json match {
      case JString(x) => Validation.success(DateTime.parse(x))
      case x => UnexpectedJSONError(x, classOf[JString]).failureNel
    }
  }

    implicit val userDataFormat: JSON[UserData] = new JSON[UserData] {
    def read(json: JValue) = (UserData.apply _).applyJSON(
        field[String]("username"),
        field[DateTime]("expiryDate"))(json)

    def write(data: UserData) = JObject(
        "username" -> data.username.toJson,
        "expiryDate" -> data.expiryDate.toJson)
  }


  val crypto = Crypto("E27D^[_<Lpt0vjad]de;3;tx3gpRmG4ByofnahOIo9gbsMWut1w3xg[>9W")

  val isSigned = true

  val name = "LOGGED_IN_USER_COMPUTER_APP"

  val discarding: Cookie = {
    Cookie(
        name = name,
        value = "",
        path = Some("/"),
        maxAge = Some(0))
  }

  def encode(userData: UserData): Cookie = {
    val json = userData.toJson
    val string = if (isSigned) {
      val toSign = json.shows
      val hash = crypto.sign(toSign)
      JObject(
        "data" -> json,
        "hash" -> JString(hash)
      ).shows
    } else
      json.shows
    Cookie(
        name = name,
        value = java.net.URLEncoder.encode(string, "UTF-8"),
        path = Some("/"),
        maxAge = Some(Minutes.minutes(5).toStandardSeconds().getSeconds()))
  }

  def decode(data: String): Option[UserData] = {
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
        if (safeEquals((json \ "hash").convertTo[String].getOrElse(""), crypto.sign(data.shows)))
          data.convertTo[UserData].toOption
        else
          None
      } else (json \ "username").convertTo[UserData].toOption
    } catch {
      // fail gracefully is the session cookie is corrupted
      case _: Exception => None
    }

  }

}
