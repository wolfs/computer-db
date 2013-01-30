package filter

import slick.session.{Session, Database}
import unfiltered.filter.Plan

trait Db {
  def db: Database
}

trait Transactional { self: Db =>

  type SessionIntent = Session => Plan.Intent

  def transactional(sessionIntent: SessionIntent): Plan.Intent = {
    case x => db.withTransaction { session =>
      sessionIntent(session)(x)
    }
  }

  def transactional(intent: Plan.Intent): Plan.Intent = {
    case x => db.withTransaction { intent(x) }
  }

}
