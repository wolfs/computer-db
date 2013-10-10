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


trait ComputerServiceComponent extends CompanyResource with ComputerResource { self: RepositoryComponent with DatabaseAccess with Transactional =>

  class ComputerServiceActor extends HttpServiceActor
    with CompanyService
    with ComputerService
    with LoginService
    with MyFileAndResourceDirectives {
    def receive = runRoute(companyRoute ~ computerRoute ~ loginRoute ~
    get {
      getFromDirectory("../angular-frontend/target/generated-web/public")
    })
  }

}