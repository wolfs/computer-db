package dal

import java.util.Date
import models._
import scala.slick.lifted._
import scala.slick.direct.AnnotationMapper.table
import scala.slick.driver.ExtendedProfile
import scala.slick.session.Session
import util.SequenceId

trait Profile {
  val profile: ExtendedProfile
}

class DAL(override val profile: ExtendedProfile) extends ComputerComponent with Profile

trait ComputerComponent { self: Profile =>
  import profile.simple._

  object TypeMapper {
    type SqlDate = java.sql.Date
    private val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    implicit val sqlDate2Date = MappedTypeMapper.base[Date, SqlDate](
      dt => java.sql.Date.valueOf(dateFormat.format(dt)),
      sdt => dateFormat.parse(sdt.toString)
    )

  }

  object Companies extends Table[Company]("COMPANY") with SequenceId {
    def id = column[Option[Long]]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def * = id ~ name <> (Company.apply _, Company.unapply _)

    def optIdToCompany(idOpt: Option[Option[Long]], nameOpt: Option[String]): Option[Company] ={
      for {
        id <- idOpt
        name <- nameOpt
      } yield Company(id, name)
    }

    def optCompanyToOptId(companyOpt: Option[Company]): Option[(Option[Option[Long]], Option[String])] = {
      (for {
        company <- companyOpt
      } yield Company.unapply(company).map { case (x,y) => (Some(x), Some(y))}).getOrElse(Some((None, None)))
    }

    def *? = id.? ~ name.? <> (optIdToCompany _, optCompanyToOptId _)

    def insert(model: Company)(implicit db: Session) = {
      val modelToInsert = model.id.map { x => model} getOrElse {
        model.copy(id = nextId)
      }
      *.insert(modelToInsert)
      modelToInsert
    }
    def seqName: String = "COMPANY"

    def list(implicit db: Session): Seq[Company] = {
      Query(Companies).list
    }
  }

  object Computers extends Table[Computer]("COMPUTER") with SequenceId {
    import TypeMapper._
    def id = column[Option[Long]]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def introduced = column[Option[Date]]("INTRODUCED")
    def discontinued = column[Option[Date]]("DISCONTINUED")
    def companyId = column[Option[Long]]("COMPANY_ID")
    def company = foreignKey("FK_COMPUTER_COMPANY_1", companyId, Companies)(_.id)
    def * = id ~ name ~ introduced ~ discontinued ~ companyId <> (Computer.apply _, Computer.unapply _)

    def insert(model: Computer)(implicit db: Session) = {
      val modelToInsert = model.id.map { x => model} getOrElse {
        model.copy(id = nextId)
      }
      *.insert(modelToInsert)
      modelToInsert
    }

    def seqName: String = "COMPUTER"

    def list(implicit db: Session): Seq[Computer] = {
      allQuery.list
    }

    lazy val idQuery = for {
      id <- Parameters[Long]
      computer <- Computers if computer.id is id
    } yield { computer }

    lazy val allQuery = Query(Computers)

    def findById(id: Long)(implicit db: Session): Option[Computer] = {
      idQuery(id).firstOption
    }

    def idQueryDyn(id: Long) = {
      Query(Computers).where(_.id === id)
    }

    def update(model: Computer)(implicit db: Session) = {
       idQueryDyn(model.id.get).update(model)
    }

    def delete(id: Long)(implicit db: Session) = {
      idQueryDyn(id).delete
    }

    def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%", descending: Boolean = false)(implicit db: Session): Page[(Computer, Option[Company])] = {

      val offset = pageSize * page

      val query = for {
        (computer, company) <- Computers leftJoin Companies on (_.companyId === _.id)
        if computer.name like filter
      } yield (computer, company)

      def ascOrDesc(c: Column[_]) = {
        if (descending) {
          c.desc.nullsLast
        } else {
          c.asc.nullsLast
        }
      }

      def column: ((Computers.type, Companies.type)) => Column[_] = orderBy match {
        case 1 => _._1.name
        case 2 => _._1.introduced
        case 3 => _._1.discontinued
        case 4 => _._2.name
      }

      val sortedQueryWithOffset = query.sortBy(column andThen ascOrDesc).drop(offset).take(pageSize)
      val computers = for {
        (computer, company) <- sortedQueryWithOffset
      } yield (computer, company.*?)
      val total = (for (c <- Computers if c.name like filter) yield (c.length)).first
      Page(computers.list, page, offset, total)
    }
  }
}
