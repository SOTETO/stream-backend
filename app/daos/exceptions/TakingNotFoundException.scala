package daos.exceptions

import java.util.UUID

case class TakingNotFoundException(uuid: UUID) extends DatabaseException {
  override def getMessage: String =
    "Taking with id " + uuid + "' has not been found."
}
