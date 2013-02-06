package filter

import dal.DAL
import unfiltered.filter.Plan
import unfiltered.request.{GET, Path}
import unfiltered.response.{Pass, ResponseString}
import scala.slick.session.Database
import scala.slick.session.Session
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
import filter.Transactional._
import unfiltered.Cycle
import Authentication._

class CompanyPlan(dal: DAL, val db: Database) extends Transactional with Db {
  import models.MyJsonProtocol._


  val Seg = PrefixSeg("api", "companies")

  def intent: Cycle.Intent[Any, Any] = auth {
    case GET(Path(Seg(Nil))) => { implicit session: Session =>
      val companies = dal.Companies.list
      ResponseString(companies.toJson.shows)
    }
    case req@Path(Seg(id :: Nil)) => req match {
      case GET(_) => { implicit session: Session =>
        import dal.profile.simple._
        val company = (for {
          company <- dal.Companies
          if (company.id === id.toLong)
        } yield { company }).list.head
        ResponseString(company.toJson.shows)
      }
      case _ => Pass
    }
  }
}