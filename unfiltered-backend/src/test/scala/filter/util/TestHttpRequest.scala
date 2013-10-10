package filter.util

import unfiltered.request.HttpRequest
import java.io.InputStream
import java.io.Reader

case class FakeHttpRequest(
  val inputStream: InputStream = null,
  val reader: Reader = null,
  val protocol: String = null,
  val method: String,
  val uri: String,
  val parameters: Map[String, Seq[String]] = Map(),
  val headers: Map[String, Iterator[String]] = Map(),
  val isSecure: Boolean = false,
  val remoteAddr: String = "localhost"
) extends HttpRequest[Any](null) {
  def parameterNames: Iterator[String] = parameters.keys.iterator
  def parameterValues(param: String) : Seq[String] = parameters.getOrElse(param, Seq())
  
  def headerNames: Iterator[String] = headers.keys.iterator
  def headers(name: String) : Iterator[String] = headers.getOrElse(name, Seq().iterator)

}