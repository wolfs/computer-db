package filter

import org.scalatra.swagger.{NativeSwaggerBase, Swagger, SwaggerBase}
import org.scalatra.ScalatraServlet
import com.fasterxml.jackson.databind._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.swagger.JacksonSwaggerBase
import org.scalatra.ScalatraFilter

class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet with JacksonSwaggerBase {
  override implicit val jsonFormats: Formats = DefaultFormats

}

class ComputersSwagger extends Swagger("1.0", "1")