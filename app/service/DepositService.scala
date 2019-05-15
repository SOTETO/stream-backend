package services



import java.util.UUID
import daos.{DepositDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Deposit
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._


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
  
  def create(deposit: Deposit): Future[Option[Deposit]] = ???

  def update(deposit: Deposit): Future[Option[Deposit]] = ???

  def read(uuid: UUID): Future[Option[Deposit]] = ???
  def delete(uuid: UUID): Future[Boolean] = ???

}
