package util

import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import spray.http._

import MediaTypes._
import org.json4s.scalaz.JsonScalaz._
import org.json4s.scalaz._
import scalaz._
import Scalaz._
import org.json4s._
import org.json4s.JsonAST.JValue
import util.json._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._


trait Json4sSupport {

  def resultToEither[T](result: Result[T]): Deserialized[T] = {
      result.fold(e => Left(MalformedContent(e.toString)), s => Right(s))
  }

  implicit def sprayJsonParser =
    new SimpleUnmarshaller[JValue] {
    val canUnmarshalFrom = Seq(ContentTypeRange(`application/json`))
    def unmarshal(entity: HttpEntity) = {
        protect(parse(entity.asString))
    }
  }

  implicit def sprayJsonUnmarshaller[T :JSONR] =
    new SimpleUnmarshaller[T] {
    val canUnmarshalFrom = Seq(ContentTypeRange(`application/json`))
    def unmarshal(entity: HttpEntity) = {
      for {
        json <- sprayJsonParser(entity)
        entity <- resultToEither(jsonReader[T].read(json))
      } yield { entity }
    }
  }

  implicit def sprayJsonMarshaller[T: JSONW] =
    Marshaller.delegate[T, String](ContentType.`application/json`) { value =>
      val json = jsonWriter[T].write(value)
      compact(render(json))
    }
}

object Json4sSupport extends Json4sSupport