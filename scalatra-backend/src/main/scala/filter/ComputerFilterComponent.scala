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
import org.scalatra.swagger._
import org.scalatra.json.JacksonJsonSupport

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

  class ComputerFilter(implicit val swagger: Swagger) extends ScalatraFilter
  with JacksonJsonSupport
  with UrlGeneratorSupport
  with CookieAuthSupport
  with SwaggerSupport {
    protected override val jsonFormats = DefaultFormats

    override protected val applicationName = Some("computers")
    protected val applicationDescription = "The computer-database API. It exposes operations for browsing and searching lists of computers, and retrieving single computers."

    before() {
      if (requestPath.startsWith("/api/") && !requestPath.startsWith("/api/login/")) {
        cookieAuth
      }
    }

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

    val getComputers =
    (apiOperation[String]("getComputers")
      summary "Show all computers"
      notes "Shows all the computers in the computer-db. This view is paginated, sorted and can be filtered"
      parameter queryParam[Option[Int]]("p").description("The current page to show. Starts at 0")
      parameter queryParam[Option[String]]("f").description("The current filter to apply.")
      parameter queryParam[Option[Int]]("s").description("Sort by which column.")
      parameter queryParam[Option[Boolean]]("d").description("Sort descending.")
      )

    get("/api/computers/?", operation(getComputers)) {
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