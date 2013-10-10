package util

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.http.HttpCookie
import spray.http.HttpHeaders.`Set-Cookie`

@RunWith(classOf[JUnitRunner])
class CookieAuthenticationTest extends FunSuite with CookieAuthentication {

  test("Cookie is serialized without quotation marks") {
    println(`Set-Cookie`(HttpCookie(name="Test", content="whatever", path = Some("/"))).value)
  }

}