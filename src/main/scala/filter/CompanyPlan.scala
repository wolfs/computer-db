package filter

import models.DAL
import unfiltered.filter.Plan
import unfiltered.request.{GET, Path}
import unfiltered.response.{Pass, ResponseString}
import scala.slick.session.Database
import scala.slick.session.Session
import filter.util._
import spray.json._

class CompanyPlan(dal: DAL) {
  import models.MyJsonProtocol._

  val Seg = PrefixSeg("api", "companies")

  def intent(implicit session: Session): Plan.Intent = {
    case GET(Path(Seg(Nil))) => {
      ResponseString(dal.Companies.list.toJson.compactPrint)
    }
    case req@Path(Seg(id :: Nil)) => req match {
      case GET(_) => {
        import dal.profile.simple._
        val company = (for {
          company <- dal.Companies
          if (company.id === id.toLong)
        } yield { company }).list.head
        ResponseString(company.toJson.compactPrint)
      }
      case _ => Pass
    }
  }
}