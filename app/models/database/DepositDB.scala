package models.database

import models.frontend.{ Deposit, DepositUnit }
import slick.driver.MySQLDriver.api._
import java.util.UUID


/**
 * Implements the Deposit database representation. 
 * 
 *  """""""""""          """""""""""""""
 *  " Deposit "  1 <---n " DepositUnit "
 *  """""""""""          """""""""""""""
 */




/**
 * DepositUnit Database representation
 *
 */

case class DepositUnitDB(
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

object DepositUnitDB extends ((Long, String, Long, Double, Long, Long) => DepositUnitDB){
  def apply(tuple: (Long, String, Long, Double, Long, Long)): DepositUnitDB = 
    DepositUnitDB(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6)

 // def apply()
}

class DepositUnitTableDef(tag: Tag) extends Table[DepositUnitDB](tag, "Deposit_Unit") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("public_id")
  def received = column[Long]("received")
  def amount = column[Double]("amount")
  def created = column[Long]("created")
  def depositId = column[Long]("deposit_id")

  def * =
    (id, publicId, received, amount, created, depositId) <> (DepositUnitDB.tupled, DepositUnitDB.unapply)
  
  def depositKey = foreignKey("deposit_id", depositId, TableQuery[DepositTableDef])(_.id, onUpdate = ForeignKeyAction.Cascade)
}

/**
 * Deposit Database representation
 */

case class DepositDB(
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
      DepositUnit(UUID.fromString(this.publicId), depositUnitList, this.state, this.crew, this.supporter, this.created, this.updated, this.dateOfDeposit)
  }

object DepositDB extends ((Long, String, String, String, Long, Long, Long) => DepositDB){
  def apply(tuple: (Long, String, String, String, Long, Long, Long)): DepositDB = 
    DepositDB(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7)
}

class DepositTableDef(tag: Tag) extends Table[DepositDB](tag, "Deposit") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("publicId")
  def state = column[String]("state")
  def crew = column[String]("crew")
  def created = column[Long]("created")
  def updated = column[Long]("updated")
  def dateOfDeposit = column[Long]("date_of_deposit")
  
  def * =
    (id, publicId, state, crew, created, updated, dateOfDeposit) <> (DepositDB.tupled, DepositDB.unapply)

}
