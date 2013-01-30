package filter

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._
import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import scala.slick.driver.H2Driver
import models._
import scala.slick.session.Database
import scala.slick.session.Session
import scala.slick.lifted.{Query => Q}
import java.io.File
import java.net.URL

object Public extends Plan with Transactional with Db {
  lazy val dal = new DAL(H2Driver)
  override lazy val db = Database.forURL("jdbc:h2:mem:computerdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "", password = "")

  lazy val companyPlan = new CompanyPlan(dal)
  lazy val computerPlan = new ComputerPlan(dal)

  override def intent: Plan.Intent =  transactional {
    implicit session: Session => {
    case req@Path(companyPlan.Seg(_)) => companyPlan.intent(session)(req)
    case req@Path(computerPlan.Seg(_)) => computerPlan.intent(session)(req)
    case req@Path(Login.Seg(_)) => Login.intent(req)
    case _ => Pass
    }
  }

  def main(args: Array[String]) {
    val flyway = new Flyway();
    val dataSource = new DriverDataSource("org.h2.Driver", "jdbc:h2:mem:computerdb;DB_CLOSE_DELAY=-1", "", "");
    flyway.setDataSource(dataSource)
    flyway.migrate()
    val here = new File("here")
    unfiltered.jetty.Http.local(53333).resources(new URL(here.toURI().toURL(),"../angular-frontend/_public")).filter(Public).run()
  }

}
