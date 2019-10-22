package service

import daos.exceptions.TakingAddException
import daos.{TakingsDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Taking
import play.api.Configuration
//import testdata.TakingTestData
import utils.{TakingFilter, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

class TakingsService @Inject() (config: Configuration, dao: TakingsDAO, implicit val userDAO: UserDAO) {
  implicit val ec = ExecutionContext.global
  /*TakingTestData(config).init(20)
    .flatMap(list => Future.sequence(list.map(donation => save(donation))))
*/
  def all(page: Option[Page], sort: Option[Sort], filter: Option[TakingFilter]) : Future[List[Taking]] = dao.all(page, sort, filter)
  def count(filter: Option[TakingFilter]) : Future[Int] = dao.count(filter)
  def save(donation: Taking): Future[Either[TakingAddException, Taking]] = dao.save(donation)
}
