package daos.reader
import java.util.UUID

import models.frontend._
import slick.jdbc.GetResult

case class TakingsReader(
  id: Long,
  publicId: UUID,
  received: Long,
  description: String,
  category: String,
  comment: Option[String],
  reason_for_payment: Option[String],
  receipt: Option[Boolean],
  author: UUID,
  crew: UUID,
  created: Long,
  updated: Long
  ) {
  def toTakings(
    supporter: Seq[InvolvedSupporterReader] = Nil,
    sources: Seq[SourceReader] = Nil,
    
    )
  }
