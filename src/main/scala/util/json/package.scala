package util

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.JsonAST.JValue

/**
 * @author wolfs
 */
package object json {
  def jsonReader[T](implicit reader: JSONR[T]) = reader
  def jsonWriter[T](implicit writer: JSONW[T]) = writer

  implicit def pimpAny[T](any: T) = new PimpedAny(any)
  implicit def pimpString(string: String) = new PimpedString(string)
  implicit def pimpJson(json: JValue) = new PimpedJson(json)
}

package json {


  private[json] class PimpedAny[T](any: T) {
    def toJson(implicit writer: JSONW[T]): JValue = writer.write(any)
  }

  private[json] class PimpedString(string: String) {
    def asJson: JValue = parse(string)
  }

  private[json] class PimpedJson(json: JValue) {
    def convertTo[T](implicit reader: JSONR[T]) = reader.read(json)
  }
}