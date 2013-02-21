package util

import spray.routing.directives.FileAndResourceDirectives
import java.io.File
import spray.routing._
import akka.actor.ActorRefFactory
import spray.routing.directives._
import spray.routing.directives.MethodDirectives._
import spray.httpx.marshalling.BasicMarshallers
import spray.util._
import org.parboiled.common.FileUtils
import spray.http.{DateTime => SDateTime, _}
import StatusCodes._
import spray.http.parser.ProtocolParameterRules
import spray.http.parser.AdditionalRules
import org.parboiled.scala._
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import org.joda.time.DateTimeZone

trait MyFileAndResourceDirectives extends FileAndResourceDirectives {
  import BasicDirectives._
  import ExecutionDirectives._
  import MethodDirectives._
  import RespondWithDirectives._
  import RouteDirectives._
  import MiscDirectives._
  import HeaderDirectives._
  import FileAndResourceDirectives.{stripLeadingSlash, withTrailingSlash}

  val RFC1123Pattern = DateTimeFormat
      .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
      .withLocale(Locale.US)
      .withZone(DateTimeZone.UTC);

  override def getFromFile(file: File)(implicit settings: RoutingSettings, resolver: ContentTypeResolver,
                  refFactory: ActorRefFactory): Route =
    get {
      optionalHeaderValueByName("If-Modified-Since") { value =>
      detachTo(singleRequestServiceActor) {
        val lastModified = file.lastModified
        val modifiedSince = value.map(RFC1123Pattern.parseDateTime(_).getMillis < lastModified) getOrElse(true)
        if (!modifiedSince) {
          complete(NotModified)
        } else {
          respondWithLastModifiedHeader(file.lastModified) {
            if (file.isFile && file.canRead) {
              implicit val bufferMarshaller = BasicMarshallers.byteArrayMarshaller(resolver(file.getName))
              if (0 < settings.FileChunkingThresholdSize && settings.FileChunkingThresholdSize <= file.length)
                complete(file.toByteArrayStream(settings.FileChunkingChunkSize.toInt))
              else complete(FileUtils.readAllBytes(file))
            } else reject
          }
        }
      }
      }
    }
}