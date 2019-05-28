package daos.reader

import java.util.UUID

import models.frontend._
import slick.jdbc.GetResult

case class DonationReader(
                         id: Option[Long],
                         publicId: UUID,
                         received: Long,
                         description: String,
                         category: String,
                         comment: Option[String],
                         reason_for_payment: Option[String],
                         receipt: Option[Boolean],
                         author: UUID,
                         created: Long,
                         updated: Long
                         ) {
  def toDonation(supporter: List[UUID] = Nil, sources: List[Source] = Nil) : Donation =
    Donation(
      publicId,
      DonationAmount(received, supporter, sources),
      Context(description, category),
      comment,
      reason_for_payment.flatMap(rfp => receipt.map(r => Details(rfp, r))),
      author,
      created,
      updated
    )
}

object DonationReader extends ((Option[Long], UUID, Long, String, String, Option[String], Option[String], Option[Boolean], UUID, Long, Long) => DonationReader ) {

  def apply(donation: Donation): DonationReader =
    DonationReader(
      None,
      donation.id,
      donation.amount.received,
      donation.context.description,
      donation.context.category,
      donation.comment,
      donation.details.map(_.reasonForPayment),
      donation.details.map(_.receipt),
      donation.author,
      donation.created,
      donation.updated
    )

  def apply(tuple: (Option[Long], String, Long, String, String, Option[String], Option[String], Option[Boolean], String, Long, Long)): DonationReader =
    DonationReader(tuple._1, UUID.fromString(tuple._2), tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, UUID.fromString(tuple._9), tuple._10, tuple._11)

  def unapply(arg: DonationReader): Option[(Option[Long], String, Long, String, String, Option[String], Option[String], Option[Boolean], String, Long, Long)] =
    Some((arg.id, arg.publicId.toString, arg.received, arg.description, arg.category, arg.comment, arg.reason_for_payment, arg.receipt, arg.author.toString, arg.created, arg.updated))

  implicit val getDonationReader = GetResult(r =>
    DonationReader(r.nextLongOption, UUID.fromString(r.nextString), r.nextLong, r.nextString, r.nextString, r.nextStringOption, r.nextStringOption, r.nextBooleanOption, UUID.fromString(r.nextString), r.nextLong, r.nextLong)
  )
}