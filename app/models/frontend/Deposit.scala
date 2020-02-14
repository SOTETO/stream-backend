package models.frontend

import daos.reader.{DepositReader, DepositUnitReader}
import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._


/** Represents an amount
 *  @param amount how much money
 *  @param currency Euro Dollar
 */
case class Amount(amount: Double, currency: String)
/** Factory for [[Amount]] instance. Can be handle as Json */
object Amount {
  implicit val amountFormat = Json.format[Amount]
}

/**
  * Handles Json for creating [[DepositUnit]].
  * @param takingId public_id of [[models.frontend.Taking]]
  * @param confirmed date as Long
  * @param amount amount represented by [[Amount]] 
  * @param created date as Long
  */
case class DepositUnitStub(
  takingId: UUID,
  confirmed: Option[Confirmed],
  amount: Amount,
  created: Long
  ) {
    /**
     * Create [[DepositUnit]] with random [[java.util.UUID]]
     * @return
     */
    def toDepositUnit(): DepositUnit =
      DepositUnit(UUID.randomUUID(), this.takingId, None, this.confirmed, this.amount, this.created)
  }

/** Factory for [[DepositUnitStub]] instance. Can be handle as Json */
object DepositUnitStub {
  implicit val depositUnitStubFormat = Json.format[DepositUnitStub]
}

/**
 * Represents `amount` of a [[Deposit]] 
 * @param publicId
 * @param takingId
 * @param confirmed
 * @param amount
 * @param created
 */
case class DepositUnit(
  publicId: UUID,
  takingId: UUID,
  description: Option[String],
  confirmed: Option[Confirmed],
  amount: Amount,
  created: Long
)

/** Factory for [[DepositUnit]] instance. Can be handle as Json */
object DepositUnit {
  implicit val depositUnitFormat = Json.format[DepositUnit]
}

/**
 * Handle the Json for creating [[Deposit]]
 * @param full
 * @param amount
 * @param confirmed
 * @param crew
 * @param supporter
 * @param created
 * @param updated
 * @param dateOfDeposit
 */
case class DepositStub(
  full: Amount,
  amount: List[DepositUnitStub],
  confirmed: Option[Confirmed],
  crew: InvolvedCrew,
  supporter: InvolvedSupporter,
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
/** Factory for [[DepositStub]] instance. Can be handle as Json */
object DepositStub {
  implicit val depositStubFormat = Json.format[DepositStub]
}

/**
 * Represents deposits as Json
 * @param publicId 
 * @param full
 * @param amount
 * @param confirmed
 * @param crew
 * @param supporter
 * @param created
 * @param updated
 * @param dateOfDeposit
 */
case class Deposit(
  publicId: UUID,
  full: Amount,
  amount: List[DepositUnit],
  confirmed: Option[Confirmed],
  crew: InvolvedCrew,
  supporter: InvolvedSupporter,
  created: Long,
  updated: Long,
  dateOfDeposit: Long
  )
/** Factory for [[DepositStub]] instance. Can be handle as Json */
object Deposit{
  implicit val depositFormat = Json.format[Deposit]
}

/** A class for filter 
 * @constructor Create a new DepositFilter with `publicId`, `takingsId` and `crew`
 * @param publicId
 * @param takingsId
 * @param crew
 */
case class DepositFilter(
  publicId: Option[UUID], 
  takingsId: Option[UUID],
  crew: Option[UUID],
  name: Option[List[String]],
  afrom: Option[Double],
  ato: Option[Double],
  confirmed: Option[Boolean],
  cby: Option[UUID],
  cfrom: Option[Long],
  cto: Option[Long],
  payfrom: Option[Long],
  payto:Option[Long]
) 
{
  /** Extend a deposit filter with given crew_id
   * @param crewId public_id of a Crew as UUID
   * @return with crew extended deposit filter
   */
  def extend(crewId: UUID): DepositFilter = DepositFilter(this.publicId, this.takingsId, Some(crewId), this.name, this.afrom, this.ato, this.confirmed, this.cby, this.cfrom, this.cto, this.payfrom, this.payto)
}

/** Factory for [[DepositFilter]] instance. Can be handle as Json */
object DepositFilter {
  implicit val depositFilterFormat = Json.format[DepositFilter]
  /** Creates a DepositFilter with given crewId
   *  @param crewId
   */
  def apply(crewId: UUID) : DepositFilter = DepositFilter(None, None, Some(crewId), None, None, None, None, None, None, None, None, None)
}
/** Represents the request query for deposits
 * @param page 
 * @param sort
 * @param filter
 */
case class DepositQueryBody(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter])

/**Factory for [[DepositQueryBody]] instance. Can handle as json.*/
object DepositQueryBody {
    implicit val depositQueryBodyFormat = Json.format[DepositQueryBody]
  }
