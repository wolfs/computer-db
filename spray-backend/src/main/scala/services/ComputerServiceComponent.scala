package services

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import models.DatabaseAccess
import util.Transactional
import models.RepositoryComponent
import models.MyJsonProtocol._
import _root_.util.json._
import util.Json4sSupport
import models.Computer



trait ComputerServiceComponent { self: RepositoryComponent with DatabaseAccess with Transactional =>

  class ComputerServiceActor extends Actor with HttpServiceActor with CompanyService with ComputerService {
    def receive = runRoute(companyRoute ~ computerRoute ~
    get {
      getFromDirectory("../angular-frontend/target/generated-web/public")
    })
  }


  trait CompanyService extends Directives with Json4sSupport {

    val companyRoute =
      path("api/companies") {
        get {
          complete {
            withTransaction { implicit session =>
              Companies.list
            }
          }
        }
      }
  }

  trait ComputerService extends Directives with Json4sSupport {
    val computerRoute =
      pathPrefix("api/computers") {
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
                  Computers.insert(computer)
                }
              }
            }
          }} ~
        path(LongNumber) { id =>
          get {
            complete {
              withTransaction { implicit session =>
                Computers.findById(id)
              }
            }
          } ~
          put {
            entity(as[Computer]) { computer =>
              complete { withTransaction { implicit session =>
                Computers.update(computer.copy(id = Some(id)))
              }}
            }
          } ~
          delete {
            complete {
              withTransaction { implicit session =>
                Computers.delete(id)
              }
            }
          }
        }
    }
  }
}