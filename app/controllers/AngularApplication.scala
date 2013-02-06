package controllers

import play.api._
import libs.json.Json
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import templates.Html
import views._
import models._
import controllers.routes.javascript._
import scala.xml.NodeSeq

/**
 * @author wolfs
 */
object AngularApplication extends Controller with Secured {

  def check(username: String, password: String) = {
    (username == "admin" && password == "1234")
  }

  def login(username: String) = Action(BodyParsers.parse.json) { implicit request =>
    val body = request.body
    (for {
      password <- (body \ "password").asOpt[String] if check(username, password)
    } yield NoContent.withCookies(UsernameCookie.encodeAsCookie(Some(Username(username))))).getOrElse(BadRequest("Invalid username or password"))
  }

  def logout(user: String) = withAuthAndLogout { loggedInUser => implicit request =>
    if (user == loggedInUser) Ok else BadRequest("You cannot logout another user")
  }

}
