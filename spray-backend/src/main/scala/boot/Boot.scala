package boot

import akka.actor.Props
import akka.actor.actorRef2Scala
import dal.H2DbAccess
import dal.SlickDal
import scala.slick.driver.H2Driver
import util.Transactional
import services.ComputerServiceComponent
import akka.actor.ActorSystem
import spray.can.Http
import akka.io.IO


object Boot extends App with H2DbAccess {
  lazy val app = new SlickDal(H2Driver, db)
    with Transactional
    with ComputerServiceComponent

  implicit val system = ActorSystem()
  // create and start our service actor
  val service = system.actorOf(Props(new app.ComputerServiceActor()), "computer-service")

  migrate

  // create a new HttpServer using our handler tell it where to bind to
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 54444)

}