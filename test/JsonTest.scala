/**
 * @author wolfs
 */

import anorm.NotAssigned
import org.specs2.mutable._
import models._
import play.api.libs.json.{JsUndefined, JsResult, Json}

class JsonTest extends Specification {

  "Json Reader" should {

    "read a Computer" in {
      import Implicits._
      val computerJson = Json.obj(
        "name" -> "Test Computer",
        "companyId" -> 37
      )
      val computer: JsResult[Computer] = Json.fromJson[Computer](computerJson)
      println(computer)
      computer.get.name must equalTo("Test Computer")
      computer.get.id must equalTo(NotAssigned)
    }

    "write a Computer" in {
      import Implicits._
      val computer = Computer(NotAssigned, "Test", None, None, None)
      val json = Json.toJson(computer)

      (json \ "name").as[String] must equalTo("Test")
      (json \ "id") must beAnInstanceOf[JsUndefined]
    }

    "write and read a computer" in {
      import Implicits._
      val computer = Computer(NotAssigned, "Test", None, None, None)

      Json.fromJson[Computer](Json.toJson(computer)).get must equalTo(computer)
    }

  }

}
