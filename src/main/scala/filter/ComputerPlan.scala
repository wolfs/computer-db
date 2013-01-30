package filter

import models.DAL
import util.PrefixSeg
import slick.session.Session
import unfiltered.filter.Plan
import unfiltered.request.{GET, Path, Method}
import unfiltered.response._
import spray.json._
import unfiltered.request.Params
import scala.util.control.Exception.allCatch
import unfiltered.request.Body
import unfiltered.request.PUT
import models.Computer
import unfiltered.request.DELETE
import unfiltered.request.POST
import scala.Some
import models.Computer
import unfiltered.response.ResponseString

class ComputerPlan(dal: DAL) {
  import models.MyJsonProtocol._
  val Seg = PrefixSeg("api", "computers")

  def notFoundOrNoContent(num: Int) = {
    if (num == 0) NotFound else NoContent
  }

  def boolean(in: Option[String]): Option[Boolean] = {
    in.flatMap { s => allCatch.opt (s.toBoolean)}
  }

  object Page extends Params.Extract("p", Params.first ~> Params.int)
  object Filter extends Params.Extract("f", Params.first)
  object Sort extends Params.Extract("s", Params.first ~> Params.int)
  object Descending extends Params.Extract("d", Params.first ~> boolean)

  def intent(implicit session: Session): Plan.Intent = {
  case req@Path(Seg(Nil)) => req match {
    case GET(_) => {
      val Params(params) = req
      val page = Page.unapply(params).getOrElse(0)
      val filter = Filter.unapply(params).getOrElse("")
      val filterString = s"%${filter}%"
      val sort = Sort.unapply(params).getOrElse(1)
      val desc = Descending.unapply(params).getOrElse(false)
      val computers = dal.Computers.list(
          page = page,
          filter = filterString,
          orderBy = sort,
          descending = desc)
      ResponseString(computers.toJson.compactPrint)
    }
    case POST(_) => {
      val computer = Body.string(req).asJson.convertTo[Computer]
      val insertedComputer = dal.Computers.insert(computer)
      Created ~> Location(s"/api/computers/${insertedComputer.id.get}")
    }
    case _ => Pass
  }
  case req@Path(Seg(idString :: Nil)) => {
    try {
      val id = idString.toLong;

      req match {
      case GET(_) => {
//        val computer = dal.Computers.findById(id)
//        computer.map(c => ResponseString(c.toJson.compactPrint)).getOrElse(NotFound)
        Unauthorized
      }
      case PUT(_) => {
        val computer = Body.string(req).asJson.convertTo[Computer]
        notFoundOrNoContent(dal.Computers.update(computer.copy(id = Some(id))))
      }
      case DELETE(_) => {
        notFoundOrNoContent(dal.Computers.delete(id))
      }
      case _ => Pass
      }
    } catch {
      case e: NumberFormatException => NotFound
    }
  }
  case _ => Pass
  }
}
