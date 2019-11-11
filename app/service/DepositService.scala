package services



import java.util.UUID

import daos.exceptions.{DatabaseException, DepositAddException}
import daos.{DepositDAO, UserDAO}
import javax.inject.Inject
import models.frontend.{Deposit, DepositFilter, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._



/**
 * Deposit Service
 */
class DepositService @Inject() (dao: DepositDAO, userDAO: UserDAO) {
  def find(uuid: UUID): Future[Option[Deposit]] = dao.find(uuid)
  def create(deposit: Deposit): Future[Either[DatabaseException, Deposit]] = dao.create(deposit)

  def update(deposit: Deposit): Future[Option[Deposit]] = ???

  def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter]):Future[List[Deposit]] = dao.all(page, sort, filter)
  def count(filter: Option[DepositFilter] = None): Future[Int] = dao.count(filter)
  def confirm(uuid: UUID, date: Long): Future[Boolean] = dao.confirm(uuid, date)

  def delete(uuid: UUID): Future[Boolean] = ???

}
