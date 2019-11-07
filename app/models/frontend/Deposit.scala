package models.frontend

import daos.reader.{DepositReader, DepositUnitReader}
import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._



/**
  * Used for creating a [[DepositUnit]].
  */
case class DepositUnitStub(
  takingId: UUID,
  confirmed: Option[Long],
  amount: Double,
  currency: String,
  created: Long
  ) {
    /**
     * Create [[DepositUnit]] with random [[java.util.UUID]]
     * @return
     */
    def toDepositUnit(): DepositUnit =
      DepositUnit(UUID.randomUUID(), this.takingId, this.confirmed, this.amount, this.currency, this.created)
  }

object DepositUnitStub {
  implicit val depositUnitStubFormat = Json.format[DepositUnitStub]
}


/* Amount Unit Json
 * Contains the uuid of the taking and 
 * the amount of the deposit in relation to the taking
 */

/**
 * Represents one part of a [[Deposit]] 
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
 * Used for creating [[Deposit]]
 */
case class DepositStub(
  full: FullAmount,
  amount: List[DepositUnitStub],
  confirmed: Option[Long],
  crew: UUID,
  supporter: UUID,
  created: Long,
  updated: Long,
  dateOfDeposit: Long
  ) {
    /**
     * Create [[Deposit]] with random [[java.util.UUID]]
     * @return
     */
    def toDeposit(): Deposit = {
      val depositUnitList: List[DepositUnit] = this.amount.map(_.toDepositUnit())
      Deposit(
        UUID.randomUUID(), 
        this.full, 
        this.amount.map(_.toDepositUnit),  //transform List[DepositUnitStub] to List[DepositUnit]
        this.confirmed, 
        this.crew, 
        this.supporter, 
        this.created, 
        this.updated, 
        this.dateOfDeposit
      )
    }
  }
object DepositStub {
  implicit val depositStubFormat = Json.format[DepositStub]
}

/**
 * Represents a deposit from a user or BankAccount
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
