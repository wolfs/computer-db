package services

import models.RepositoryComponent
import models.DatabaseAccess
import util.Transactional
import spray.routing.Directives
import util.CookieAuthentication
import util.Json4sSupport
import models.MyJsonProtocol._

trait CompanyResource { self: RepositoryComponent with DatabaseAccess with Transactional =>

  trait CompanyService extends Directives with CookieAuthentication with Json4sSupport {

    val companyRoute =
      path("api" / "companies") {
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
}