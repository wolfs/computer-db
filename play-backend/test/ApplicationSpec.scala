package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.mvc.WithHeaders
import controllers.UsernameCookie
import controllers.Username
import models.Implicits._
import play.api.http.HeaderNames

class ApplicationSpec extends Specification {
  
  import models._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
  
  // --
  
  def loggedInFakeRequest = FakeRequest().withCookies(UsernameCookie.encodeAsCookie(Some(Username("admin"))))
  
  "Application" should {
    
    "list computers on the the first page" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = controllers.Computers.list(0, 2, "")(loggedInFakeRequest)

        status(result) must equalTo(OK)
        val json = contentAsJson(result)
        (json \ "page").as[Int] must equalTo(0)
        (json \ "total").as[Int] must equalTo(574)
      }      
    }
    
    "filter computer by name" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val result = controllers.Computers.list(0, 2, "Apple")(loggedInFakeRequest)

        status(result) must equalTo(OK)
        (contentAsJson(result) \ "total").as[Int] must equalTo(13)
        
      }      
    }
    
    "create new computer" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val badResult = controllers.Computers.save(loggedInFakeRequest).run
        
        status(badResult) must equalTo(BAD_REQUEST)
        
        val badDateFormat = controllers.Computers.save(
          loggedInFakeRequest.
          withJsonBody(Json.toJson(Map("name" -> "FooBar", "companyId" -> "1"))).
          withHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
        ).run
        
        status(badDateFormat) must equalTo(BAD_REQUEST)
        contentAsString(badDateFormat) must contain("""<option value="1" selected>Apple Inc.</option>""")
        contentAsString(badDateFormat) must contain("""<input type="text" id="introduced" name="introduced" value="badbadbad" >""")
        contentAsString(badDateFormat) must contain("""<input type="text" id="name" name="name" value="FooBar" >""")
        
        val result = controllers.Computers.save(
          FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "2011-12-24", "companyId" -> "1")
        ).run
        
        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/computers")
        flash(result).get("success") must beSome.which(_ == "Computer FooBar has been created")
        
        val list = controllers.Computers.list(0, 2, "FooBar")(FakeRequest())

        status(list) must equalTo(OK)
        contentAsString(list) must contain("One computer found")
        
      }
    }
    
  }
  
}