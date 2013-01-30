package filter.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import unfiltered.request.Seg

@RunWith(classOf[JUnitRunner])
class PrefixSeqTest extends FunSuite {
  test("Does not match other prefix") {
    val MyPrefix = PrefixSeg("test")
        val thing = "test2/123" match {
        case MyPrefix("123" :: Nil) => false
        case _ => true
    }
    assert(thing)
  }
  test("Matches correct prefix") {
    val MyPrefix = PrefixSeg("test")
    val thing = "test/123" match {
        case MyPrefix("123" :: Nil) => true
        case _ => false
    }
    assert(thing)
  }
  test("Matches correct prefixes") {
    val MyPrefix = PrefixSeg("api", "test")
    val thing = "api/test/123" match {
    case MyPrefix("123" :: Nil) => true
    case _ => false
    }
    assert(thing)
  }
}