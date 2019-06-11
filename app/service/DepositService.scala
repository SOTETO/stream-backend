package services



import java.util.UUID
import daos.{DepositDAO, UserDAO}
import javax.inject.Inject
import models.frontend.{DepositFilter, Deposit}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import utils.{Page, Sort}



/**
 * Deposit Service
 */
class DepositService @Inject() (dao: DepositDAO, userDAO: UserDAO) {

/*  private def prepare(filter: Option[HouseholdFilter]): Future[Option[HouseholdFilter]] =
    this.dao.all(None, None, None).flatMap(households => {
      // get ids of all referenced users inside the database!
      val userIds = households.filter(_.versions.head.author.isDefined).map(_.versions.head.author.get).distinct
      // get all user (UUIDs) of the same crew as the given one by the filter
      filter.map(hf => hf.crew match {
        case Some(id) => userDAO.crewSupporter(userIds, id).map(supporter =>
          hf >> supporter
        )
        case _ => Future.successful(hf)
      }) match {
        // Option[Future[...]] to Future[Option[...]]
        case Some(f) => f.map(Some(_))
        case None => Future.successful(None)
      }})*/
  

  def find(uuid: UUID): Future[Option[Deposit]] = dao.find(uuid)
  def create(deposit: Deposit): Future[Option[Deposit]] = dao.create(deposit)

  def update(deposit: Deposit): Future[Option[Deposit]] = ???

  def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter]):Future[Option[List[Deposit]]] = ???
  def count(): Future[Option[Int]] = ???

  def read(uuid: UUID): Future[Option[Deposit]] = ???
  def delete(uuid: UUID): Future[Boolean] = ???

}
