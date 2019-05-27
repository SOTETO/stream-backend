package daos.reader

import java.util.UUID

import slick.jdbc.GetResult

case class InvolvedSupporterReader(
                                  id: Long,
                                  donation_id: Long,
                                  supporter: UUID
                                  ) {
  def toUUID = supporter
}

object InvolvedSupporterReader extends ((Long, Long, UUID) => InvolvedSupporterReader ) {
  def apply(tuple: (Long, Long, String)): InvolvedSupporterReader =
    InvolvedSupporterReader(tuple._1, tuple._2, UUID.fromString(tuple._3))

  def unapply(arg: InvolvedSupporterReader): Option[(Long, Long, String)] =
    Some((arg.id, arg.donation_id, arg.supporter.toString))

  implicit val getInvolvedSupporterReader = GetResult(r =>
    InvolvedSupporterReader(r.nextLong, r.nextLong, UUID.fromString(r.nextString))
  )
}