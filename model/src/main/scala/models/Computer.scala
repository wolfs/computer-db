package models

import java.util.Date
import org.json4s.scalaz.JsonScalaz._
import org.json4s.scalaz._
import scala.Some
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.JsonAST.JValue
import util.json._

case class Company(
                    id: Option[Long] = None,
                    name: String)

case class Computer(id: Option[Long] = None, name: String, introduced: Option[Date], discontinued: Option[Date], companyId: Option[Long])

/**
 * Helper for pagination.
 */
case class Page[A](items: List[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object MyJsonProtocol {
  implicit def seqJSON[A: JSON]: JSON[Seq[A]] = new JSON[Seq[A]] {
    def write(values: Seq[A]) = JArray(values.map(x => toJSON(x)).toList)

    def read(json: JValue) = json match {
      case JArray(xs) =>
        xs.map(fromJSON[A]).sequence[({type λ[t] = ValidationNEL[Error, t]})#λ, A]
      case x => UnexpectedJSONError(x, classOf[JArray]).failureNel
    }
  }


  implicit val companyFormat: JSON[Company] = new JSON[Company] {
    def read(json: JValue) = (Company.apply _).applyJSON(
        field[Option[Long]]("id"),
        field[String]("name"))(json)

    def write(data: Company) = JObject(
        "id" -> data.id.toJson,
        "name" -> data.name.toJson)
  }
  private val dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd")
  implicit val dateFormat: JSON[Date] = new JSON[Date] {
    def write(value: Date): JValue = {
      JString(dateFormatter.format(value))
    }

    def read(json: JValue): Result[Date] = json match {
      case JString(x) => Validation.success(dateFormatter.parse(x))
      case x => UnexpectedJSONError(x, classOf[JString]).failureNel
    }
  }
  implicit val computerFormat = new JSON[Computer] {
    def read(json: JValue) = Computer.applyJSON(
      field[Option[Long]]("id"),
      field[String]("name"),
      field[Option[Date]]("introduced"),
      field[Option[Date]]("discontinued"),
      field[Option[Long]]("companyId"))(json)

    def write(data: Computer) = JObject(
        "id" -> data.id.toJson,
        "name" -> data.name.toJson,
        "introduced" -> data.introduced.toJson,
        "discontinued" -> data.discontinued.toJson,
        "companyId" -> data.companyId.toJson
        )
  }

  implicit val computerCompanyFormat: JSON[(Computer, Option[Company])] = new JSON[(Computer, Option[Company])] {
    def read(json: JValue) = ((x: Computer,y: Option[Company]) => (x,y)).applyJSON(
        field[Computer]("computer"),
        field[Option[Company]]("company"))(json)

    def write(data: (Computer, Option[Company])) = JObject(
        "computer" -> data._1.toJson,
        "company" -> data._2.toJson
        )
  }


  implicit def pageFormat[T: JSON]: JSON[Page[T]] = new JSON[Page[T]] {
    def read(json: JValue) = (Page.apply[T] _).applyJSON(
      field[List[T]]("items"),
      field[Int]("page"),
      field[Long]("offset"),
      field[Long]("total")
      )(json)

    def write(data: Page[T]) = JObject(
        "items" -> data.items.toJson,
        "page" -> data.page.toJson,
        "offset" -> data.offset.toJson,
        "total" -> data.total.toJson)
  }
}


