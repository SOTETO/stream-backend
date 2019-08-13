package models.frontend

import java.util.UUID

case class Takings(
  publicID: UUID,
  amount: Amount,  
  context: Context,
  comment: Option[String],
  details: Option[String],
  depositUnit: List[DepositUnit],
  author: UUID,
  crew: UUID,
  created: Long,
  updated: Long
  )

object Takings {
  implicit val takingsFormat = Json.format[Takings]
}
