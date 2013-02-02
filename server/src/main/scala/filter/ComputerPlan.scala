package filter

import models.DAL
import util.PrefixSeg
import slick.session.Session
import unfiltered.filter.Plan
import unfiltered.request.{GET, Path, Method}
import unfiltered.response._
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
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.native.scalaz._
import _root_.util.json._
import Transactional._

class ComputerPlan(dal: DAL) {
  import models.MyJsonProtocol._
  val Seg = PrefixSeg("api", "computers")

  def notFoundOrNoContent(num: Int) = {
    if (num == 0) NotFound else NoContent
  }

  def badResultOnError[A,B](result: Result[A])(f: A => ResponseFunction[B]) = {
    result.fold(
        x => BadRequest ~> ResponseString(x.toString),
        x => f(x)
        )
  }

  def boolean(in: Option[String]): Option[Boolean] = {
    in.flatMap { s => allCatch.opt (s.toBoolean)}
  }

  object Page extends Params.Extract("p", Params.first ~> Params.int)
  object Filter extends Params.Extract("f", Params.first)
  object Sort extends Params.Extract("s", Params.first ~> Params.int)
  object Descending extends Params.Extract("d", Params.first ~> boolean)

  def intent: TransactionalIntent[Any, Any] = {
  case req@Path(Seg(Nil)) => req match {
    case GET(_) => { implicit session =>
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
      ResponseString(computers.toJson.shows)
    }
    case POST(_) => {  implicit session =>
      val computerResult = Body.string(req).asJson.convertTo[Computer]
      badResultOnError(computerResult){ computer =>
          val insertedComputer = dal.Computers.insert(computer)
          Created ~> Location(s"/api/computers/${insertedComputer.id.get}")
      }
    }
    case _ => Pass
  }
  case req@Path(Seg(idString :: Nil)) => {  implicit session =>
    try {
      val id = idString.toLong;

      req match {
      case GET(_) => {
        val computer = dal.Computers.findById(id)
        computer.map(c => ResponseString(c.toJson.shows)).getOrElse(NotFound)
      }
      case PUT(_) => {
        val computerResult = Body.string(req).asJson.convertTo[Computer]
        badResultOnError(computerResult){ computer =>
            notFoundOrNoContent(dal.Computers.update(computer.copy(id = Some(id))))
        }
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
