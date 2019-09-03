package service

import daos.exceptions.DonationAddException
import daos.{DonationsDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Donation
import play.api.Configuration
//import testdata.DonationTestData
import utils.{DonationFilter, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

class DonationsService @Inject() (config: Configuration, dao: DonationsDAO, implicit val userDAO: UserDAO) {
  implicit val ec = ExecutionContext.global
  /*DonationTestData(config).init(20)
    .flatMap(list => Future.sequence(list.map(donation => save(donation))))
*/
  def all(page: Option[Page], sort: Option[Sort], filter: Option[DonationFilter]) : Future[List[Donation]] = dao.all(page, sort, filter)
  def count(filter: Option[DonationFilter]) : Future[Int] = dao.count(filter)
  def save(donation: Donation): Future[Either[DonationAddException, Donation]] = dao.save(donation)
}
