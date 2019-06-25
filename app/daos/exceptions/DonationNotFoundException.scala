package daos.exceptions

import java.util.UUID

case class DonationNotFoundException(uuid: UUID) extends DatabaseException {
  override def getMessage: String =
    "Donation with id " + uuid + "' has not been found."
}
