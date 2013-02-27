package services

import akka.actor.Actor
import spray.routing._
import spray.http.{DateTime => SDateTime, _}
import StatusCodes._
import MediaTypes._
import models.DatabaseAccess
import util.Transactional
import models.RepositoryComponent
import models.MyJsonProtocol._
import _root_.util.json._
import util.Json4sSupport
import models.Computer
import util.UsernameCookie
import util.UserData
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import shapeless._
import util.CookieAuthentication
import spray.routing.directives.CompletionMagnet
import util.MyFileAndResourceDirectives
import spray.http.HttpHeaders.Location


trait ComputerServiceComponent { self: RepositoryComponent with DatabaseAccess with Transactional =>

  class ComputerServiceActor extends Actor
    with HttpServiceActor
    with CompanyService
    with ComputerService
    with LoginService
    with MyFileAndResourceDirectives {
    def receive = runRoute(companyRoute ~ computerRoute ~ loginRoute ~
    get {
      getFromDirectory("../angular-frontend/target/generated-web/public")
    })
  }


  trait CompanyService extends Directives with CookieAuthentication with Json4sSupport {

    val companyRoute =
      path("api/companies") {
        get {
          cookieAuth { userData =>
            complete {
              withTransaction { implicit session =>
                Companies.list
              }
            }
          }
        }
      }
  }

  trait ComputerService extends Directives  with CookieAuthentication with Json4sSupport {

    def notFoundOrNoContent(num: Int): CompletionMagnet = {
      if (num == 0) {
        NotFound
      } else {
        NoContent
      }
    }

    val computerRoute =
      (pathPrefix("api/computers") & cookieAuth) { userData =>
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