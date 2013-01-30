package models

import java.util.Date
import slick.direct.AnnotationMapper._
import slick.direct.Queryable
import slick.session.Session
import scala.slick.jdbc.{StaticQuery => Q}
import slick.driver.ExtendedProfile
import spray.json._
import slick.direct.AnnotationMapper.column
import scala.Some
import slick.direct.AnnotationMapper.table
import slick.lifted.BaseTypeMapper

@table("company")
case class Company(
                    @column("id") id: Option[Long] = None,
                    @column("name") name: String)

object Company {
  val companies = Queryable[Company]
}

case class Computer(id: Option[Long] = None, name: String, introduced: Option[Date], discontinued: Option[Date], companyId: Option[Long])

trait Profile {
  val profile: ExtendedProfile
}


class DAL(override val profile: ExtendedProfile) extends CompanyComponent with Profile {
}

trait WithSequence {
  def seqName: String
  def nextId(implicit db: Session) =
    Some((Q[Long] + s"select nextval('${seqName}_SEQ')").first)
}

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val companyFormat = jsonFormat2(Company.apply)
  private val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
  implicit object DateJsonFormat extends JsonFormat[Date] {
    def write(obj: Date): JsValue = {
      JsString(dateFormat.format(obj))
    }

    def read(json: JsValue): Date = json match {
      case JsString(dateString) => dateFormat.parse(dateString)
      case _ => throw new DeserializationException("String expected")
    }
  }
  implicit val computerFormat = jsonFormat5(Computer.apply)
  implicit val pairFormat = jsonFormat((_: Computer,_: Option[Company]), "computer", "company")
  implicit def pageFormat[A: JsonFormat] = jsonFormat(Page.apply[A], "items", "page", "offset", "total")
}


