package models

import java.util.Date

case class Company(
                    id: Option[Long] = None,
                    name: String)

case class Computer(id: Option[Long] = None, name: String, introduced: Option[Date], discontinued: Option[Date], companyId: Option[Long])

/**
 * Helper for pagination.
 */
case class Page[A](items: List[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

trait DatabaseAccess {
  type Session
  def withTransaction[T](f: Session => T): T
}

trait RepositoryComponent { self: DatabaseAccess =>

  val Companies: Companies

  val Computers: Computers

  trait Companies {
    def insert(model: Company)(implicit db: Session): Company

    def list(implicit db: Session): Seq[Company]
  }

  trait Computers {
    def insert(model: Computer)(implicit db: Session): Computer
    def findById(id: Long)(implicit db: Session): Option[Computer]
    def update(model: Computer)(implicit db: Session): Int
    def delete(id: Long)(implicit db: Session): Int
    def list(
        page: Int = 0,
        pageSize: Int = 10,
        orderBy: Int = 1,
        filter: String = "%",
        descending: Boolean = false)(implicit db: Session): Page[(Computer, Option[Company])]


  }
}


