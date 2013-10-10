package controllers

import play.api._
import libs.json.Json
import play.api.mvc._
import models.{Company, Implicits}

/**
 * @author wolfs
 */
object Companies extends Controller {

  def companyJson = AuthenticatedAction {
    import Implicits._
    val company = Company(name = "Stuff")
    Ok(Json.toJson(company))
  }

  def list = AuthenticatedAction {
    import Implicits._
    Ok(Json.toJson(Company.list))
  }

}
