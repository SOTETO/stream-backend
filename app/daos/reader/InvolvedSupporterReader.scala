package daos.reader

import java.util.UUID

import models.frontend.Taking
import slick.jdbc.GetResult

case class InvolvedSupporterReader(
                                  id: Option[Long],
                                  taking_id: Long,
                                  supporter: UUID
                                  ) {
  def toUUID = supporter
}

object InvolvedSupporterReader extends ((Option[Long], Long, UUID) => InvolvedSupporterReader ) {

  def apply(supporter: UUID, taking_id: Long): InvolvedSupporterReader =
    InvolvedSupporterReader(None, taking_id, supporter)

  def apply(tuple: (Option[Long], Long, String)): InvolvedSupporterReader =
    InvolvedSupporterReader(tuple._1, tuple._2, UUID.fromString(tuple._3))

  def unapply(arg: InvolvedSupporterReader): Option[(Option[Long], Long, String)] =
    Some((arg.id, arg.taking_id, arg.supporter.toString))

  implicit val getInvolvedSupporterReader = GetResult(r =>
    InvolvedSupporterReader(r.nextLongOption, r.nextLong, UUID.fromString(r.nextString))
  )
}
