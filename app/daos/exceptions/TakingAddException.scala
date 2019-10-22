package daos.exceptions

import models.frontend.Taking

case class TakingAddException(donation: Taking) extends DatabaseException {
  override def getMessage: String =
    "Taking with id " + donation.id + " and name '" + donation.context.description + "' (created " + donation.created + ") has not been saved."
}
