package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.{DepositReader, DepositUnitReader}
import slick.lifted.Tag
import utils.{Ascending, Descending, Sort}


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
  def confirmed = column[Long]("confirmed")
  def amount = column[Double]("amount")
  def currency = column[String]("currency")
  def created = column[Long]("created")
  def depositId = column[Long]("deposit_id")
  def donationId = column[Long]("donation_id")

  def * =
    (id, publicId, confirmed.?, amount, currency, created, depositId, donationId) <> (DepositUnitReader.tupled, DepositUnitReader.unapply)

  def pk = primaryKey("primaryKey", id)
  
  def depositKey = foreignKey("deposit_id", depositId, TableQuery[DepositTable])(_.id, onUpdate = ForeignKeyAction.Cascade)
  def donationKey = foreignKey("donation_id", donationId, TableQuery[DonationTable])(_.id, onUpdate = ForeignKeyAction.Cascade)
}

class DepositTable(tag: Tag) extends Table[DepositReader](tag, "Deposit") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("public_id")
  def fullAmount = column[Double]("full_amount")
  def currency = column[String]("currency")
  def confirmed = column[Long]("confirmed")
  def crew = column[String]("crew")
  def supporter = column[String]("supporter")
  def created = column[Long]("created")
  def updated = column[Long]("updated")
  def dateOfDeposit = column[Long]("date_of_deposit")
  
  def * =
    (id, publicId, fullAmount, currency, confirmed.?, crew, supporter, created, updated, dateOfDeposit) <> (DepositReader.tupled, DepositReader.unapply)

  def pk = primaryKey("primaryKey", id)

  def sortBy(sort: Sort) = {
    sort.field match {
      case "public_id" => Some(sort.dir match {
        case Descending => this.publicId.desc.nullsFirst
        case Ascending => this.publicId.asc.nullsFirst
        case _ => this.publicId.asc.nullsFirst
      })
      case "full_amount" => Some(sort.dir match {
        case Descending => this.fullAmount.desc.nullsFirst
        case Ascending => this.fullAmount.asc.nullsFirst
        case _ => this.fullAmount.asc.nullsFirst
      })
      case "currency" => Some(sort.dir match {
        case Descending => this.currency.desc.nullsFirst
        case Ascending => this.currency.asc.nullsFirst
        case _ => this.currency.asc.nullsFirst
      })
      case "confirmed" => Some(sort.dir match {
        case Descending => this.confirmed.desc.nullsFirst
        case Ascending => this.confirmed.asc.nullsFirst
        case _ => this.confirmed.asc.nullsFirst
      })
      case "crew" => Some(sort.dir match {
        case Descending => this.crew.desc.nullsFirst
        case Ascending => this.crew.asc.nullsFirst
        case _ => this.crew.asc.nullsFirst
      })
      case "supporter" => Some(sort.dir match {
        case Descending => this.supporter.desc.nullsFirst
        case Ascending => this.supporter.asc.nullsFirst
        case _ => this.supporter.asc.nullsFirst
      })
      case "date_of_deposit" => Some(sort.dir match {
        case Descending => this.dateOfDeposit.desc.nullsFirst
        case Ascending => this.dateOfDeposit.asc.nullsFirst
        case _ => this.dateOfDeposit.asc.nullsFirst
      })
      case "created" => Some(sort.dir match {
        case Descending => this.created.desc.nullsFirst
        case Ascending => this.created.asc.nullsFirst
        case _ => this.created.asc.nullsFirst
      })
      case "updated" => Some(sort.dir match {
        case Descending => this.updated.desc.nullsFirst
        case Ascending => this.updated.asc.nullsFirst
        case _ => this.updated.asc.nullsFirst
      })
      case _ => None
    }
  }
}
