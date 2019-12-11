package daos.reader

import java.util.UUID

import models.frontend.InvolvedSupporter
import slick.jdbc.GetResult

case class InvolvedSupporterReader(
                                  id: Option[Long],
                                  taking_id: Long,
                                  uuid: UUID,
                                  name: String
                                  ) {
  def toUUID: InvolvedSupporter = InvolvedSupporter(uuid, name)
}

object InvolvedSupporterReader extends ((Option[Long], Long, UUID, String) => InvolvedSupporterReader ) {

  def apply(supporter: InvolvedSupporter, taking_id: Long): InvolvedSupporterReader =
    InvolvedSupporterReader(None, taking_id, supporter.uuid, supporter.name)

  def apply(tuple: (Option[Long], Long, String, String)): InvolvedSupporterReader =
    InvolvedSupporterReader(tuple._1, tuple._2, UUID.fromString(tuple._3), tuple._4)

  def unapply(arg: InvolvedSupporterReader): Option[(Option[Long], Long, String, String)] =
    Some((arg.id, arg.taking_id, arg.uuid.toString, arg.name))

  implicit val getInvolvedSupporterReader = GetResult(r =>
    InvolvedSupporterReader(r.nextLongOption, r.nextLong, UUID.fromString(r.nextString), r.nextString)
  )
}
