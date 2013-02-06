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
object Computers extends Controller with Secured {
  def list(page: Int, orderBy: Int, filter: String) = authenticated { implicit Request =>
    import Implicits._
    Ok(Json.toJson(Computer.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%"))))
  }

  def save = authenticated(BodyParsers.parse.json) { implicit request =>
    import Implicits._
    Json.fromJson[Computer](request.body).asOpt.map { computer =>
      val id = Computer.insert(computer)
      Created.withHeaders(LOCATION -> routes.Application.edit(id).toString())
    }.getOrElse(BadRequest("Json did not validate"))
  }

  def update(id: Long) = authenticated(BodyParsers.parse.json) { implicit request =>
    import Implicits._
    Json.fromJson[Computer](request.body).asOpt.map { computer =>
      Computer.update(id, computer)
      NoContent
    }.getOrElse(BadRequest("Json did not validate"))
  }

  def find(id: Long) = authenticated { implicit request =>
    import Implicits._
    Computer.findById(id).
      map { computer: Computer => Ok(Json.toJson(computer)) }.getOrElse {
      NotFound
    }
  }

  def delete(id: Long) = authenticated { implicit request =>
    Computer.delete(id)
    NoContent
  }

}

