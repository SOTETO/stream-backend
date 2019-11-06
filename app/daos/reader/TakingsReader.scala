package daos.reader

import java.util.UUID

import models.frontend._
import slick.jdbc.GetResult

case class TakingReader(
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
  def toTaking(
                  supporter: Seq[InvolvedSupporterReader] = Nil,
                  sources: Seq[SourceReader] = Nil,
                  depositUnits: Seq[DepositUnitReader] = Nil
                ) : Taking =
    Taking(
      publicId,
      TakingAmount(
        received,
        supporter.filter(_.taking_id == id).map(_.toUUID).toList.distinct,
        sources.filter(_.taking_id == id).map(_.toSource).toList.distinct
      ),
      Context(description, category),
      norms,
      comment,
      reason_for_payment.flatMap(rfp => receipt.map(r => Details(rfp, r))),
      depositUnits.filter(_.takingId == id).map(_.toDepositUnit(publicId)).toList.distinct,
      author,
      crew,
      created,
      updated
    )
}

object TakingReader extends ((Long, UUID, Long, String, String, String, Option[String], Option[String], Option[Boolean], UUID, UUID, Long, Long) => TakingReader ) {

  def apply(taking: Taking, id: Option[Long] = None): TakingReader =
    TakingReader(
      id.getOrElse(0),
      taking.id,
      taking.amount.received,
      taking.context.description,
      taking.context.category,
      taking.norms,
      taking.comment,
      taking.details.map(_.reasonForPayment),
      taking.details.map(_.receipt),
      taking.author,
      taking.crew,
      taking.created,
      taking.updated
    )

  def apply(tuple: (Long, String, Long, String, String, String, Option[String], Option[String], Option[Boolean], String, String, Long, Long)): TakingReader =
    TakingReader(tuple._1, UUID.fromString(tuple._2), tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, tuple._9, UUID.fromString(tuple._10), UUID.fromString(tuple._11), tuple._12, tuple._13)

  def unapply(arg: TakingReader): Option[(Long, String, Long, String, String, String, Option[String], Option[String], Option[Boolean], String, String, Long, Long)] =
    Some((arg.id, arg.publicId.toString, arg.received, arg.description, arg.category, arg.norms, arg.comment, arg.reason_for_payment, arg.receipt, arg.author.toString, arg.crew.toString, arg.created, arg.updated))

  implicit val getTakingReader = GetResult(r =>
    TakingReader(r.nextLong, UUID.fromString(r.nextString), r.nextLong, r.nextString, r.nextString, r.nextString, r.nextStringOption, r.nextStringOption, r.nextBooleanOption, UUID.fromString(r.nextString), UUID.fromString(r.nextString), r.nextLong, r.nextLong)
  )
}
