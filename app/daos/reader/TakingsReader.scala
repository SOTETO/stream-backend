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
                         comment: Option[String],
                         reason_for_payment: Option[String],
                         receipt: Option[Boolean],
                         author: UUID,
                         created: Long,
                         updated: Long
                         ) {
  def toTaking(
                  supporter: List[InvolvedSupporter] = Nil,
                  sources: List[Source] = Nil,
                  depositUnits: List[DepositUnit] = Nil,
                  involvedCrew: List[InvolvedCrew] = Nil
                ) : Taking =
    Taking(
      publicId,
      TakingAmount(
        received,
        supporter.distinct,
        sources
      ),
      Context(description, category),
      comment,
      reason_for_payment.flatMap(rfp => receipt.map(r => Details(rfp, r))),
      depositUnits,
      author,
      involvedCrew.distinct,
      created,
      updated
    )
}

object TakingReader extends ((Long, UUID, Long, String, String, Option[String], Option[String], Option[Boolean], UUID, Long, Long) => TakingReader ) {

  def apply(taking: Taking, id: Option[Long] = None): TakingReader =
    TakingReader(
      id.getOrElse(0),
      taking.id,
      taking.amount.received,
      taking.context.description,
      taking.context.category,
      taking.comment,
      taking.details.map(_.reasonForPayment),
      taking.details.map(_.receipt),
      taking.author,
      taking.created,
      taking.updated
    )

  def apply(tuple: (Long, String, Long, String, String, Option[String], Option[String], Option[Boolean], String, Long, Long)): TakingReader =
    TakingReader(tuple._1, UUID.fromString(tuple._2), tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, UUID.fromString(tuple._9), tuple._10, tuple._11)

  def unapply(arg: TakingReader): Option[(Long, String, Long, String, String, Option[String], Option[String], Option[Boolean], String, Long, Long)] =
    Some((arg.id, arg.publicId.toString, arg.received, arg.description, arg.category, arg.comment, arg.reason_for_payment, arg.receipt, arg.author.toString, arg.created, arg.updated))

  implicit val getTakingReader = GetResult(r =>
    TakingReader(r.nextLong, UUID.fromString(r.nextString), r.nextLong, r.nextString, r.nextString, r.nextStringOption, r.nextStringOption, r.nextBooleanOption, UUID.fromString(r.nextString), r.nextLong, r.nextLong)
  )
}
