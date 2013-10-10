package boot

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.DefaultServlet
import filter.HelloWorldServlet
import java.net.URL
import java.io.File
import org.eclipse.jetty.util.resource.Resource
import dal.SlickDal
import filter.ComputerFilterComponent
import scala.slick.driver.H2Driver
import dal.H2DbAccess
import javax.servlet.Filter
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.ServletContextHandler
import javax.servlet.DispatcherType
import java.util.EnumSet
import org.eclipse.jetty.servlet.ServletHolder
import javax.servlet.Servlet
import filter.LoginFilter
import filter.ResourcesApp
import filter.ComputersSwagger
import org.scalatra.servlet.ServletApiImplicits._

object ScalatraBoot extends H2DbAccess {

  lazy val app = new SlickDal(H2Driver, db)
  with ComputerFilterComponent

  def addFilter(filter: Filter, current: ServletContextHandler, path: String = "/*") {
    val holder = new FilterHolder(filter)
    holder.setName(filter.getClass().getName())
    current.addFilter(holder, path, EnumSet.noneOf(classOf[DispatcherType]))
  }

  def main(args: Array[String]) {

    migrate

    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 52222

    val here = new File("here")

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setBaseResource(Resource.newResource(new URL(here.toURI().toURL(),"../angular-frontend/target/generated-web/public")))

    implicit val swagger = new ComputersSwagger

    addFilter(new app.CompanyFilter, context)
    addFilter(new app.ComputerFilter, context)
    addFilter(new LoginFilter, context)
    val holder = new ServletHolder()
    holder.setServlet(new ResourcesApp)
    holder.setName("Stuff")
    holder.setInitOrder(10)
    context.addServlet(holder, "/api-docs/*")
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start
    server.join
  }
}