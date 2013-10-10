package filter.util

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import unfiltered.response.ComposeResponse
import unfiltered.response.JsonContent
import unfiltered.response.ResponseString
import unfiltered.response.JsContent


object Json {
  def jsonToString(json: JValue) = compact(render(json))

  def apply(json: JValue) =
    new ComposeResponse(JsonContent ~> ResponseString(jsonToString(json)))

  def apply(json: JValue, cb: String) =
    new ComposeResponse(JsContent ~> ResponseString(s"${cb}(${jsonToString(json)})"))
}