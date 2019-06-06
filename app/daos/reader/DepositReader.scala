package daos.reader

import models.frontend.{ Deposit, DepositUnit }
import java.util.UUID





/**
 * DepositUnit database reader
 *
 */

case class DepositUnitReader(
  id: Long, 
  publicId: String,
  received: Long,
  amount: Double,
  created: Long,
  depositId: Long
  ) {
    /**
     * map database model to frontend model
     */
    def toDepositUnit : DepositUnit = DepositUnit(UUID.fromString(this.publicId), this.received, this.amount, this.created)
  }

object DepositUnitReader extends ((Long, String, Long, Double, Long, Long) => DepositUnitReader){
  def apply(tuple: (Long, String, Long, Double, Long, Long)): DepositUnitReader = 
    DepositUnitReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6)

 // def apply()
}



/**
 * Deposit Database representation
 */

case class DepositReader(
  id: Long,
  publicId: String,
  state: String,
  crew: String,
  supporter: String,
  created: Long,
  updated: Long,
  dateOfDeposit: Long
  ) {
    def toDeposit(depositUnitList: List[DepositUnit]) = 
      Deposit(UUID.fromString(this.publicId), depositUnitList, this.state, UUID.fromString(this.crew), UUID.fromString(this.supporter), this.created, this.updated, this.dateOfDeposit)
  }

object DepositReader extends ((Long, String, String, String, String, Long, Long, Long) => DepositReader){
  def apply(tuple: (Long, String, String, String, String, Long, Long, Long)): DepositReader = 
    DepositReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8)
}


