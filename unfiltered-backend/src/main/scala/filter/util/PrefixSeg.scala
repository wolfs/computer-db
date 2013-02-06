package filter.util

import unfiltered.request.{ Seg => UnSeg }
import language.reflectiveCalls

trait UnapplyPath {
      def unapply(path: String): Option[List[String]]
}

case class PrefixSeg(prefix: String, OldSeg: UnapplyPath = PrefixSeg.UnapplySeg) extends
UnapplyPath {
  def unapply(path: String): Option[List[String]] = path match {
    case OldSeg(`prefix` :: remaining) => Some(remaining)
    case _ => None
  }
}

object PrefixSeg {

  val UnapplySeg = new UnapplyPath {
    override def unapply(path: String) = UnSeg.unapply(path)
  }

  def apply(prefixes: String*): UnapplyPath = {
    prefixes.foldLeft(UnapplySeg: UnapplyPath) { (Seg, nextPrefix) => PrefixSeg(nextPrefix, Seg)}
  }
}
