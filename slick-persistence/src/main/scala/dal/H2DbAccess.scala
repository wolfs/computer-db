package dal

import com.jolbox.bonecp.BoneCPDataSource
import scala.slick.session.Database
import scala.slick.driver.H2Driver
import com.googlecode.flyway.core.Flyway

trait H2DbAccess {
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

  def migrate {
    val flyway = new Flyway();
    flyway.setDataSource(dataSource)
    flyway.migrate()
  }
}