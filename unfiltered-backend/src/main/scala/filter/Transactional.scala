package filter

import slick.session.{Session, Database}
import unfiltered.filter.Plan
import unfiltered.request.HttpRequest
import unfiltered.response.ResponseFunction
import unfiltered.Cycle

trait Db {
  def db: Database
  implicit def transactionalIntentToIntent[A,B](ti: Transactional.TransactionalIntent[A,B]): Cycle.Intent[A,B] = req => req match {
    case req if ti.isDefinedAt(req) => db.withTransaction( session => ti(req)(session) )
  }
  implicit def session2ResponseFunction[A](ti: Transactional.SessionResponseFunction[A]): ResponseFunction[A] = {
    db.withTransaction( session => ti(session) )
  }
}

trait Transactional { self: Db =>
  import Transactional._
  def transactional(sessionIntent: SessionIntent): Plan.Intent = {
    case x => db.withTransaction { session: Session =>
      sessionIntent(session)(x)
    }
  }

  def transactional(intent: Plan.Intent): Plan.Intent = {
    case x => db.withTransaction { intent(x) }
  }
}

object Transactional {
  type SessionResponseFunction[B] = Session => ResponseFunction[B]
  type TransactionalIntent[-A,-B] = PartialFunction[HttpRequest[A], Session => ResponseFunction[B]]

  type SessionIntent = Session => Plan.Intent

  implicit def responseFunction2SessionResponseFunction[A](fun: ResponseFunction[A]): (Session => ResponseFunction[A]) = { session =>
    fun
  }
}
