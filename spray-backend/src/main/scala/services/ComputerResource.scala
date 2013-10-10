package services
import spray.routing._
import spray.http.{DateTime => SDateTime, _}
import StatusCodes._
import models.DatabaseAccess
import util.Transactional
import models.RepositoryComponent
import models.MyJsonProtocol._
import util.Json4sSupport
import models.Computer
import util.CookieAuthentication
import spray.routing.directives.CompletionMagnet
import spray.http.HttpHeaders.Location

trait ComputerResource { self: RepositoryComponent with DatabaseAccess with Transactional =>
  trait ComputerService extends Directives  with CookieAuthentication with Json4sSupport {

    def notFoundOrNoContent(num: Int): CompletionMagnet = {
      if (num == 0) {
        NotFound
      } else {
        NoContent
      }
    }

    val computerRoute =
      (pathPrefix("api" / "computers") & cookieAuth) { userData =>
        path(Slash) {
          get {
            parameters(
                'p.as[Option[Int]],
                'f.as[Option[String]],
                's.as[Option[Int]],
                'd.as[Option[Boolean]]) { (p,f,s,d) =>
              complete {
                withTransaction { implicit session =>
                  Computers.list(
                      page = p.getOrElse(0),
                      filter = s"%${f.getOrElse("")}%",
                      orderBy = s.getOrElse(1),
                      descending = d.getOrElse(false))
                }
              }
            }
          } ~
          post {
            entity(as[Computer]) { computer =>
              complete {
                withTransaction { implicit session =>
                  val id = Computers.insert(computer).id.get
                  HttpResponse(status = Created, headers = List(Location(s"/api/computers/$id")))
                }
              }
            }
          }} ~
        path(LongNumber) { id =>
          get {
            complete {
              withTransaction { implicit session =>
                Computers.findById(id).map[CompletionMagnet](comp => comp).
                getOrElse(NotFound)
              }
            }
          } ~
          put {
            entity(as[Computer]) { computer =>
              complete { withTransaction { implicit session =>
                notFoundOrNoContent(
                    Computers.update(computer.copy(id = Some(id)))
                )
              }}
            }
          } ~
          delete {
            complete {
              withTransaction { implicit session =>
                notFoundOrNoContent(Computers.delete(id))
              }
            }
          }
        }
    }
  }
}