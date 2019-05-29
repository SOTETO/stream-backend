package service

import daos.{DonationsDAO, UserDAO}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class DonationsService @Inject() (dao: DonationsDAO, userDAO: UserDAO) {
  def all = dao.all(None, None, None)
}
