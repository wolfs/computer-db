package filter

import models._
import org.scalatra.json.NativeJsonSupport
import util.json._
import org.json4s.DefaultJsonFormats
import org.json4s.DefaultFormats
import org.scalatra.ActionResult._
import org.scalatra._
import scalaz.NonEmptyList
import org.json4s.scalaz.JsonScalaz._

trait ComputerFilterComponent { self: RepositoryComponent with DatabaseAccess =>
  import MyJsonProtocol._

  class CompanyFilter extends ScalatraFilter with NativeJsonSupport {
    protected override val jsonFormats = DefaultFormats

    get("/api/companies/?") {
      withTransaction { implicit session =>
        Companies.list.toJson
      }
    }
  }

  class ComputerFilter extends ScalatraFilter with NativeJsonSupport with UrlGeneratorSupport {
    protected override val jsonFormats = DefaultFormats

    def convertBodyTo[T: JSONR]: T = {
      val computerResult = parsedBody.convertTo[T]
      computerResult.toOption.getOrElse(halt())
    }

    val byId = get("/api/computers/:id") {
      withTransaction { implicit session =>
        Computers.findById(params("id").toLong).map(_.toJson).getOrElse(NotFound())
      }
    }
    put("/api/computers/:id") {
      val computer = convertBodyTo[Computer]
      withTransaction { implicit session =>
        val computerWithId = computer.copy(id = Some(params("id").toLong))
        val numUpdated = Computers.update(computerWithId)
        if (numUpdated == 1) {
          NoContent()
        } else {
          NotFound()
        }
      }
    }
    get("/api/computers/?") {
      val page = params.getOrElse("p", "0").toInt
      val filter = params.getOrElse("f", "")
      val sort = params.getOrElse("s", "1").toInt
      val descending = params.getOrElse("d", "false").toBoolean

      withTransaction { implicit session =>
        Computers.list(
            page = page,
            filter = s"%${filter}%",
            orderBy = sort,
            descending = descending).toJson
        }
    }
    post("/api/computers/?") {
      val computer = convertBodyTo[Computer]
      withTransaction { implicit session =>
        val id = Computers.insert(computer)
        Created(headers = Map("Location" -> url(byId, "id" -> id.id.get.toString)))
      }
    }
  }

}