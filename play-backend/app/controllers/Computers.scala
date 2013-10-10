package controllers

import play.api._
import libs.json.Json
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.{Computer, Company, Implicits}
import views.html

/**
 * @author wolfs
 */
object Computers extends Controller {
  def list(page: Int, orderBy: Int, filter: String) = AuthenticatedAction { implicit Request =>
    import Implicits._
    Ok(Json.toJson(Computer.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%"))))
  }

  def save = AuthenticatedAction(BodyParsers.parse.json) { implicit request =>
    import Implicits._
    Json.fromJson[Computer](request.body).asOpt.map { computer =>
      val id = Computer.insert(computer)
      Created.withHeaders(LOCATION -> routes.Computers.find(id).toString())
    }.getOrElse(BadRequest("Json did not validate"))
  }

  def update(id: Long) = AuthenticatedAction(BodyParsers.parse.json) { implicit request =>
    import Implicits._
    Json.fromJson[Computer](request.body).asOpt.map { computer =>
      Computer.update(id, computer)
      NoContent
    }.getOrElse(BadRequest("Json did not validate"))
  }

  def find(id: Long) = AuthenticatedAction { implicit request =>
    import Implicits._
    Computer.findById(id).
      map { computer: Computer => Ok(Json.toJson(computer)) }.getOrElse {
      NotFound
    }
  }

  def delete(id: Long) = AuthenticatedAction { implicit request =>
    Computer.delete(id)
    NoContent
  }

}

