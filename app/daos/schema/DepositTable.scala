package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.{DepositReader, DepositUnitReader}
import slick.lifted.Tag


/**
 * Implements the Deposit database representation. 
 * 
 *  """""""""""          """""""""""""""
 *  " Deposit "  1 <---n " DepositUnit "
 *  """""""""""          """""""""""""""
 */



class DepositUnitTable(tag: Tag) extends Table[DepositUnitReader](tag, "Deposit_Unit") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("public_id")
  def received = column[Long]("received")
  def amount = column[Double]("amount")
  def created = column[Long]("created")
  def depositId = column[Long]("deposit_id")
  def donationId = column[Long]("donation_id")

  def * =
    (id, publicId, received, amount, created, depositId, donationId) <> (DepositUnitReader.tupled, DepositUnitReader.unapply)
  
  def depositKey = foreignKey("deposit_id", depositId, TableQuery[DepositTable])(_.id, onUpdate = ForeignKeyAction.Cascade)
}

class DepositTable(tag: Tag) extends Table[DepositReader](tag, "Deposit") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("publicId")
  def state = column[String]("state")
  def crew = column[String]("crew")
  def supporter = column[String]("supporter")
  def created = column[Long]("created")
  def updated = column[Long]("updated")
  def dateOfDeposit = column[Long]("date_of_deposit")
  
  def * =
    (id, publicId, state, crew, supporter, created, updated, dateOfDeposit) <> (DepositReader.tupled, DepositReader.unapply)

}
