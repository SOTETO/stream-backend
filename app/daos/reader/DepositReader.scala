package daos.reader

import models.frontend.{Deposit, DepositUnit, Amount}
import java.util.UUID

import slick.jdbc.GetResult





/**
 * A database reader for [[models.frontend.DepositUnit]]
 * @param id internal database id
 * @param publicId stringified [[java.util.UUID]]
 * @param confirmed if Deposit is confiremd, it contains a date as long
 * @param amount amount of [[models.frontend.Amount]]
 * @param currency currency of [[models.frontend.Amount]]
 * @param created date as Long
 * @param depositId foreign key for deposit table
 * @param takingId foreign key for taking table
 */
case class DepositUnitReader(
  id: Long,
  publicId: String,
  confirmed: Option[Long],
  amount: Double,
  currency: String,
  created: Long,
  depositId: Long,
  takingId: Long
  ) {
    /**
     * map [[DepositUnitReader]] to [[DepositUnit]]
     * @param takingId
     */
    def toDepositUnit(takingId: UUID) : DepositUnit = DepositUnit(UUID.fromString(this.publicId), takingId, this.confirmed, Amount(this.amount, this.currency), this.created)
  }

object DepositUnitReader extends ((Long, String, Option[Long], Double, String, Long, Long, Long) => DepositUnitReader){

//  def apply(tuple: (Long, String, Option[Long], Double, Long, Long, Long)): DepositUnitReader =
//    DepositUnitReader(tuple._1, UUID.fromString(tuple._2), tuple._3, tuple._4, tuple._5, tuple._6, tuple._7)
//
//  def unapply(arg: DepositUnitReader): Option[(Long, String, Option[Long], Double, Long, Long, Long)] =
//    Some((arg.id, arg.publicId.toString, arg.confirmed, arg.amount, arg.created, arg.depositId, arg.takingId))

  def apply(depositUnit: DepositUnit, depositId: Long, takingId: Long, id : Option[Long] = None) : DepositUnitReader =
    DepositUnitReader(
      id.getOrElse(0L),
      depositUnit.publicId.toString,
      depositUnit.confirmed,
      depositUnit.amount.amount,
      depositUnit.amount.currency,
      depositUnit.created,
      depositId,
      takingId
    )

  implicit val getDepositUnitReader = GetResult(r =>
    DepositUnitReader(r.nextLong, r.nextString, r.nextLongOption, r.nextDouble, r.nextString, r.nextLong, r.nextLong, r.nextLong)
  )
}



/**
 * Deposit Database representation
 */

case class DepositReader(
  id: Long,
  publicId: String,
  fullAmount: Double,
  currency: String,
  confirmed: Option[Long],
  crew: String,
  supporter: String,
  created: Long,
  updated: Long,
  dateOfDeposit: Long
  ) {
    def toDeposit(depositUnitList: List[DepositUnit]) = 
      Deposit(
        UUID.fromString(this.publicId),
        Amount(this.fullAmount, this.currency),
        depositUnitList,
        this.confirmed,
        UUID.fromString(this.crew),
        UUID.fromString(this.supporter),
        this.created,
        this.updated,
        this.dateOfDeposit
      )
  }

object DepositReader extends ((Long, String, Double, String, Option[Long], String, String, Long, Long, Long) => DepositReader){

//  def apply(tuple: (Long, String, Option[Long], String, String, Long, Long, Long)): DepositReader =
//    DepositReader(tuple._1, UUID.fromString(tuple._2), tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8)
//
//  def unapply(arg: DepositReader): Option[(Long, String, Option[Long], String, String, Long, Long, Long)] =
//    Some((arg.id, arg.publicId.toString, arg.confirmed, arg.crew, arg.supporter, arg.created, arg.updated, arg.dateOfDeposit))

  def apply(deposit: Deposit, id: Option[Long] = None): DepositReader =
    DepositReader(
      id.getOrElse(0L),
      deposit.publicId.toString,
      deposit.full.amount,
      deposit.full.currency,
      deposit.confirmed,
      deposit.crew.toString,
      deposit.supporter.toString,
      deposit.created,
      deposit.updated,
      deposit.dateOfDeposit
    )

  implicit val getDepositReader = GetResult(r =>
    DepositReader(r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextLongOption, r.nextString, r.nextString, r.nextLong, r.nextLong, r.nextLong)
  )
}


