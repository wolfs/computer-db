package filter

import slick.session.{Session, Database}
import unfiltered.filter.Plan
import unfiltered.request.HttpRequest
import unfiltered.response.ResponseFunction
import unfiltered.Cycle
import language.implicitConversions
import models.DatabaseAccess

trait Db {
  def db: Database
}

trait Transactional { self: DatabaseAccess =>
  def transactional(sessionIntent: SessionIntent): Plan.Intent = {
    case x => withTransaction { session: Session =>
      sessionIntent(session)(x)
    }
  }

  def transactional(intent: Plan.Intent): Plan.Intent = {
    case x => withTransaction { intent(x) }
  }
  type SessionResponseFunction[B] = Session => ResponseFunction[B]
  type TransactionalIntent[-A,-B] = PartialFunction[HttpRequest[A], Session => ResponseFunction[B]]

  type SessionIntent = Session => Plan.Intent

  implicit def responseFunction2SessionResponseFunction[A](fun: ResponseFunction[A]): (Session => ResponseFunction[A]) = { session =>
    fun
  }
  implicit def transactionalIntentToIntent[A,B](ti: TransactionalIntent[A,B]): Cycle.Intent[A,B] = req => req match {
    case req if ti.isDefinedAt(req) => withTransaction( session => ti(req)(session) )
  }
  implicit def session2ResponseFunction[A](ti: SessionResponseFunction[A]): ResponseFunction[A] = {
    withTransaction( session => ti(session) )
  }
}