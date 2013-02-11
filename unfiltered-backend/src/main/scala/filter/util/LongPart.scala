package filter.util

import scala.util.Try

object LongPart {
  def unapply(pathPart: String): Option[Long] = {
    Try(pathPart.toLong).toOption
  }
  def apply(pathPart: String) = unapply(pathPart)
}