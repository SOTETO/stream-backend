package daos.reader

import java.util.UUID

import models.frontend._
import slick.jdbc.GetResult

case class DonationReader(
                         id: Long,
                         publicId: UUID,
                         received: Long,
                         description: String,
                         category: String,
                         norms: String,
                         comment: Option[String],
                         reason_for_payment: Option[String],
                         receipt: Option[Boolean],
                         author: UUID,
                         crew: UUID,
                         created: Long,
                         updated: Long
                         ) {
  def toDonation(
                  supporter: Seq[InvolvedSupporterReader] = Nil,
                  sources: Seq[SourceReader] = Nil,
                  depositUnits: Seq[DepositUnitReader] = Nil
                ) : Donation =
    Donation(
      publicId,
      DonationAmount(
        received,
        supporter.filter(_.donation_id == id).map(_.toUUID).toList.distinct,
        sources.filter(_.donation_id == id).map(_.toSource).toList.distinct
      ),
      Context(description, category),
      norms,
      comment,
      reason_for_payment.flatMap(rfp => receipt.map(r => Details(rfp, r))),
      depositUnits.filter(_.donationId == id).map(_.toDepositUnit(publicId)).toList.distinct,
      author,
      crew,
      created,
      updated
    )
}

object DonationReader extends ((Long, UUID, Long, String, String, String, Option[String], Option[String], Option[Boolean], UUID, UUID, Long, Long) => DonationReader ) {

  def apply(donation: Donation, id: Option[Long] = None): DonationReader =
    DonationReader(
      id.getOrElse(0),
      donation.id,
      donation.amount.received,
      donation.context.description,
      donation.context.category,
      donation.norms,
      donation.comment,
      donation.details.map(_.reasonForPayment),
      donation.details.map(_.receipt),
      donation.author,
      donation.crew,
      donation.created,
      donation.updated
    )

  def apply(tuple: (Long, String, Long, String, String, String, Option[String], Option[String], Option[Boolean], String, String, Long, Long)): DonationReader =
    DonationReader(tuple._1, UUID.fromString(tuple._2), tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, tuple._9, UUID.fromString(tuple._10), UUID.fromString(tuple._11), tuple._12, tuple._13)

  def unapply(arg: DonationReader): Option[(Long, String, Long, String, String, String, Option[String], Option[String], Option[Boolean], String, String, Long, Long)] =
    Some((arg.id, arg.publicId.toString, arg.received, arg.description, arg.category, arg.norms, arg.comment, arg.reason_for_payment, arg.receipt, arg.author.toString, arg.crew.toString, arg.created, arg.updated))

  implicit val getDonationReader = GetResult(r =>
    DonationReader(r.nextLong, UUID.fromString(r.nextString), r.nextLong, r.nextString, r.nextString, r.nextString, r.nextStringOption, r.nextStringOption, r.nextBooleanOption, UUID.fromString(r.nextString), UUID.fromString(r.nextString), r.nextLong, r.nextLong)
  )
}
