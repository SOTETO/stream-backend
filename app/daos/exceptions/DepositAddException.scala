package daos.exceptions

import models.frontend.{Deposit, Donation}

case class DepositAddException(deposit: Deposit) extends DatabaseException {
  override def getMessage: String =
    "Deposit with id " + deposit.publicId + "' (created " + deposit.created + ") has not been saved."
}
