package service

import daos.exceptions.DonationAddException
import daos.{DonationsDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Donation
import play.api.Configuration
import testdata.DonationTestData
import utils.{Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

class DonationsService @Inject() (config: Configuration, dao: DonationsDAO, implicit val userDAO: UserDAO) {
  implicit val ec = ExecutionContext.global
  val testData = DonationTestData(config).init(20)
    .flatMap(list => Future.sequence(list.map(donation => save(donation))))

  def all(page: Page, sort: Sort) = dao.all(Some(page), Some(sort), None)
  def save(donation: Donation): Future[Either[DonationAddException, Donation]] = dao.save(donation)
}
