package models.frontend

import daos.reader.{DepositReader, DepositUnitReader}
import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._



/* Amount Unit Json
 * Contains the uuid of the taking and 
 * the amount of the deposit in relation to the taking
 */
case class DepositUnit(
  publicId: UUID,
  takingId: UUID,
  confirmed: Option[Long],
  amount: Double,
  currency: String,
  created: Long
)
object DepositUnit {
  implicit val depositUnitFormat = Json.format[DepositUnit]
}

case class FullAmount(amount: Double, currency: String)
object FullAmount {
  implicit val fullAmountFormat = Json.format[FullAmount]
}

/**
 * Deposit Model
 */

case class Deposit(
  publicId: UUID,
  full: FullAmount,
  amount: List[DepositUnit],
  confirmed: Option[Long],
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
