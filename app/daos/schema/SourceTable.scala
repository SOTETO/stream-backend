package daos.schema

import daos.reader.SourceReader
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import utils.{Ascending, Descending, Sort}

class SourceTable(tag: Tag) extends Table[SourceReader](tag, "Source") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def donation_id = column[Long]("donation_id")
  def category = column[String]("category")
  def amount = column[Double]("amount")
  def currency = column[String]("currency")
  def type_of_source = column[String]("type_of_source")

  val donationTable = TableQuery[DonationTable]

  def donations = foreignKey("FK_Sou_Donation", donation_id, donationTable)(
    _.id, onDelete=ForeignKeyAction.Cascade
  )

  def * = (id.?, donation_id, category, amount, currency, type_of_source) <> (SourceReader.apply, SourceReader.unapply)

  def pk = primaryKey("primaryKey", id)

  def sortBy(sort: Sort) = {
    sort.field match {
      case "donation_id" => Some(sort.dir match {
        case Descending => this.donation_id.desc.nullsFirst
        case Ascending => this.donation_id.asc.nullsFirst
        case _ => this.donation_id.asc.nullsFirst
      })
      case "category" => Some(sort.dir match {
        case Descending => this.category.desc.nullsFirst
        case Ascending => this.category.asc.nullsFirst
        case _ => this.category.asc.nullsFirst
      })
      case "amount" => Some(sort.dir match {
        case Descending => this.amount.desc.nullsFirst
        case Ascending => this.amount.asc.nullsFirst
        case _ => this.amount.asc.nullsFirst
      })
      case "currency" => Some(sort.dir match {
        case Descending => this.currency.desc.nullsFirst
        case Ascending => this.currency.asc.nullsFirst
        case _ => this.currency.asc.nullsFirst
      })
      case "type_of_source" => Some(sort.dir match {
        case Descending => this.type_of_source.desc.nullsFirst
        case Ascending => this.type_of_source.asc.nullsFirst
        case _ => this.type_of_source.asc.nullsFirst
      })
      case _ => None
    }
  }
}
