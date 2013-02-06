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
import unfiltered.Cycle
import unfiltered.filter.Planify
import filter.Transactional._
import dal.DAL

object Main extends Db {
  lazy val dal = new DAL(H2Driver)

  override lazy val db = Database.forURL("jdbc:h2:mem:computerdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "", password = "")

  lazy val companyPlan = new CompanyPlan(dal, db)
  lazy val computerPlan = new ComputerPlan(dal, db)


  def main(args: Array[String]) {
    val flyway = new Flyway();
    val dataSource = new DriverDataSource("org.h2.Driver", "jdbc:h2:mem:computerdb;DB_CLOSE_DELAY=-1", "", "");
    flyway.setDataSource(dataSource)
    flyway.migrate()
    val here = new File("here")
    unfiltered.jetty.Http.local(53333).resources(new URL(here.toURI().toURL(),"../../angular-frontend/_public"))
    .filter(Planify(companyPlan.intent))
    .filter(Planify(computerPlan.intent))
    .filter(Login).run()
  }

}
