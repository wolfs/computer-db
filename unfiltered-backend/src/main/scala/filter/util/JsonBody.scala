package filter.util

import unfiltered.request.HttpRequest
import org.json4s.native.JsonMethods._
import unfiltered.request.Body
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.native.scalaz._
import _root_.util.json._
import scala.None

object JsonBody {
  import org.json4s._

  /** @return Some(JsValue) if request contains a valid json body. */
  def apply[R: JSONR](r: HttpRequest[_]): Result[R] = try {
    convertToObject[R](parse(Body.string(r)))
  } catch { case t: Throwable => Fail("Could not parse json", t.getMessage()) }

  def convertToObject[T: JSONR](s: JValue): Result[T] = {
    s.convertTo[T]
  }
}