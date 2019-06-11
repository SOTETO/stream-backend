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
  donationId: UUID,
  received: Long,
  amount: Double,
  created: Long
)
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
  )
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
