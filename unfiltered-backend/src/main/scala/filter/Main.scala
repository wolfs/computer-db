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
import dal.H2DbAccess

object Main extends H2DbAccess {

  lazy val app = new SlickDal(H2Driver, db)
    with Transactional
    with CompanyResource
    with ComputerResource


  def main(args: Array[String]) {
    migrate
    val here = new File("here")
    val server = unfiltered.jetty.Http.local(53333)
    server.underlying.setSendDateHeader(true)
    server.resources(new URL(here.toURI().toURL(),"../angular-frontend/target/generated-web/public"))
    .filter(Planify(app.CompanyPlan.intent))
    .filter(Planify(app.ComputerPlan.intent))
    .filter(LoginPlan).run()
  }

}
