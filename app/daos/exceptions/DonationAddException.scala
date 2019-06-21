package daos.exceptions

import models.frontend.Donation

case class DonationAddException(donation: Donation) extends DatabaseException {
  override def getMessage: String =
    "Donation with id " + donation.id + " and name '" + donation.context.description + "' (created " + donation.created + ") has not been saved."
}
