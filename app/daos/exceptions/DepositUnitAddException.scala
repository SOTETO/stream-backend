package daos.exceptions

import models.frontend.DepositUnit

case class DepositUnitAddException(unit: DepositUnit) extends DatabaseException {
  override def getMessage: String =
    "DepositUnit with id " + unit.publicId + "' (created " + unit.created + ") has not been saved."
}
