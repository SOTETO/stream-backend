package models.frontend

import daos.reader.{DepositReader, DepositUnitReader}
import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._



/* Amount Unit Json
 * Contains the uuid of the donation and 
 * the amount of the deposit in relation to the donation
 */
case class DepositUnit(
  publicId: UUID,
  received: Long,
  amount: Double,
  created: Long
) {
  def toDepositUnitReader(depositId: Long) = DepositUnitReader(0, this.publicId.toString, this.received, this.amount, this.created, depositId)
}
object DepositUnit {
  implicit val depositUnitFormat = Json.format[DepositUnit]
}

/**
 * Deposit Model
 */

case class Deposit(
  publicId: UUID,
  amount: List[DepositUnit],
  state: String, //TODO
  crew: UUID,
  supporter: UUID,
  created: Long,
  updated: Long,
  dateOfDeposit: Long
  ) {
  def toDepositReader : DepositReader = DepositReader(0, this.publicId.toString, this.state, this.crew.toString, this.supporter.toString, this.created, this.updated, this.dateOfDeposit)  
}
object Deposit{
  implicit val depositFormat = Json.format[Deposit]
}

/** filter data struct for Deposit
 *
 */
case class DepositFilter(
    filter: String
)
object DepositFilter{
  implicit val depositFilterFormat = Json.format[DepositFilter]
}
