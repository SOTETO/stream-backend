package service

import daos.{DonationsDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Donation
import play.api.Configuration
import testdata.DonationTestData

import scala.concurrent.{ExecutionContext, Future}

class DonationsService @Inject() (config: Configuration, dao: DonationsDAO, implicit val userDAO: UserDAO) {
  implicit val ec = ExecutionContext.global
  val testData = DonationTestData(config).init(20)
    .flatMap(list => Future.sequence(list.map(donation => save(donation))))

  def all = dao.all(None, None, None)
  def save(donation: Donation) = dao.save(donation)
}
