package filter.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import models.FakeRepositoryCompontent
import filter.CompanyResource
import models.DatabaseAccess
import filter.Transactional
import models.FakeDatabaseAccess

@RunWith(classOf[JUnitRunner])
class CompanyPlanTest extends FunSuite{

  def testApp = new FakeRepositoryCompontent with CompanyResource with Transactional with FakeDatabaseAccess
  
}