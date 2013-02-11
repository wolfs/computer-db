package filter

import com.jolbox.bonecp.BoneCPDataSource
import unfiltered.filter.Plan
import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import scala.slick.driver.H2Driver
import scala.slick.session.Database
import java.io.File
import java.net.URL
import unfiltered.filter.Planify
import dal.SlickDal

object Main  {
  val jdbcUrl = "jdbc:h2:mem:computerdb;DB_CLOSE_DELAY=-1"
  val jdbcDriver = "org.h2.Driver"
  val jdbcUser = ""
  val jdbcPassword = ""

  lazy val dataSource = {
    val ds = new BoneCPDataSource()
    ds.setDriverClass(jdbcDriver)
    ds.setJdbcUrl(jdbcUrl)
    ds.setUsername(jdbcUser)
    ds.setPassword(jdbcPassword)
    ds
  }
  lazy val db = Database.forDataSource(dataSource)

  lazy val app = new SlickDal(H2Driver, db)
    with Transactional
    with CompanyResource
    with ComputerResource


  def main(args: Array[String]) {
    val flyway = new Flyway();
    flyway.setDataSource(dataSource)
    flyway.migrate()
    val here = new File("here")
    val server = unfiltered.jetty.Http.local(53333)
    server.underlying.setSendDateHeader(true)
    server.resources(new URL(here.toURI().toURL(),"../angular-frontend/target/generated-web/public"))
    .filter(Planify(app.CompanyPlan.intent))
    .filter(Planify(app.ComputerPlan.intent))
    .filter(Login).run()
  }

}
