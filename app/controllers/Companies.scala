package controllers

import play.api._
import libs.json.Json
import play.api.mvc._
import models.{Company, Implicits}

/**
 * @author wolfs
 */
object Companies extends Controller with Secured {

  def companyJson = authenticated {
    import Implicits._
    val company = Company(name = "Stuff")
    Ok(Json.toJson(company))
  }

  def list = authenticated {
    import Implicits._
    Ok(Json.toJson(Company.list))
  }

}
