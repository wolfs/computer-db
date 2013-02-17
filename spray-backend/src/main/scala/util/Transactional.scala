package util

import models.DatabaseAccess
import spray.routing._
import shapeless._
import spray.routing.Route
import Directives._

trait Transactional { self: DatabaseAccess =>

  object transactional extends Directive[Session :: HNil] {
    def happly(inner: (Session :: HNil => Route)): Route = {
        dynamic(withTransaction(session => inner(session :: HNil)))
    }
  }

}