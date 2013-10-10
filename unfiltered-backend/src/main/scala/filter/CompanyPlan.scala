package filter

import unfiltered.filter.Plan
import unfiltered.request.{GET, Path}
import unfiltered.response.{Pass, ResponseString}
import scala.slick.session.Database
import filter.util._
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.native.scalaz._
import _root_.util.json._
import unfiltered.response.ResponseFunction
import unfiltered.Cycle
import Authentication._
import models.RepositoryComponent
import dal.SlickDatabaseAccess
import models.DatabaseAccess

trait CompanyResource { self: RepositoryComponent with Transactional with DatabaseAccess =>
  object CompanyPlan {
    import models.MyJsonProtocol._


    val Seg = PrefixSeg("api", "companies")

    def intent: Cycle.Intent[Any, Any] = unfiltered.kit.GZip { auth {
      case GET(Path(Seg(Nil))) => { implicit session: Session =>
        val companies = Companies.list
        Json(companies.toJson)
      }
    }}
  }
}