package dal.util

import scala.slick.session.Session
import scala.slick.jdbc.{StaticQuery => Q}

trait SequenceId {
  def seqName: String
  def nextId(implicit db: Session) =
    Some((Q[Long] + s"select nextval('${seqName}_SEQ')").first)
}