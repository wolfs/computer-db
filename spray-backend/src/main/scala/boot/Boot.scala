package boot

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props
import akka.actor.actorRef2Scala
import dal.H2DbAccess
import dal.SlickDal
import scala.slick.driver.H2Driver
import util.Transactional
import services.ComputerServiceComponent


object Boot extends App with SprayCanHttpServerApp with H2DbAccess {
  lazy val app = new SlickDal(H2Driver, db)
    with Transactional
    with ComputerServiceComponent

  // create and start our service actor
  val service = system.actorOf(Props(new app.ComputerServiceActor()), "company-service")

  migrate

  // create a new HttpServer using our handler tell it where to bind to
  newHttpServer(service) ! Bind(interface = "localhost", port = 54444)

}