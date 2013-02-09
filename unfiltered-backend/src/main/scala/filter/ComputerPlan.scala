package filter

import util._
import unfiltered.filter.Plan
import unfiltered.request.{GET, Path, Method, Params}
import unfiltered.response._
import scala.util.control.Exception.allCatch
import unfiltered.request.Body
import unfiltered.request.PUT
import models.Computer
import unfiltered.request.DELETE
import unfiltered.request.POST
import models.Computer
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.native.scalaz._
import _root_.util.json._
import unfiltered.Cycle
import Authentication._
import models.RepositoryComponent
import models.DatabaseAccess

trait ComputerResource { self: RepositoryComponent with Transactional with DatabaseAccess =>
  object ComputerPlan {
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

    def intent: Cycle.Intent[Any, Any] = {
    case req@Path(Seg(Nil)) => auth {
      case GET(_) => { implicit session: Session =>
        val Params(params) = req
        val page = Page.unapply(params).getOrElse(0)
        val filter = Filter.unapply(params).getOrElse("")
        val filterString = s"%${filter}%"
        val sort = Sort.unapply(params).getOrElse(1)
        val desc = Descending.unapply(params).getOrElse(false)
        val computers = Computers.list(
            page = page,
            filter = filterString,
            orderBy = sort,
            descending = desc)
        Json(computers.toJson)
      }
      case POST(_) => {  implicit session: Session =>
        val computerResult = JsonBody[Computer](req)
        badResultOnError(computerResult){ computer =>
            val insertedComputer = Computers.insert(computer)
            Created ~> Location(s"/api/computers/${insertedComputer.id.get}")
        }
      }
      case _ => Pass
    } (req)
    case req@Path(Seg(LongPart(id) :: Nil)) => { implicit session: Session =>
      auth {
        case GET(_) => {
          val computer = Computers.findById(id)
          computer.map(c => Json(c.toJson)).getOrElse(NotFound)
        }
        case PUT(_) => {
          val computerResult = JsonBody[Computer](req)
          badResultOnError(computerResult){ computer =>
              notFoundOrNoContent(Computers.update(computer.copy(id = Some(id))))
          }
        }
        case DELETE(_) => {
          notFoundOrNoContent(Computers.delete(id))
        }
        case _ => Pass
      } (req)
    }
    case _ => Pass
    }
  }
}
