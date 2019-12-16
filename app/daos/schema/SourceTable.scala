package daos.schema

import daos.reader.SourceReader
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import utils.{Ascending, Descending, Sort}

class SourceTable(tag: Tag) extends Table[SourceReader](tag, "Source") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def public_id = column[String]("public_id")
  def taking_id = column[Long]("taking_id")
  def category = column[String]("category")
  def amount = column[Double]("amount")
  def currency = column[String]("currency")
  def type_of_source = column[String]("type_of_source")
  def type_location = column[String]("type_location")
  def type_contact_person = column[String]("type_contact_person")
  def type_email = column[String]("type_email")
  def type_address = column[String]("type_address")
  def receipt = column[Boolean]("receipt")
  def norms = column[String]("norms")

  val takingTable = TableQuery[TakingTable]

  def takings = foreignKey("FK_Sou_Taking", taking_id, takingTable)(
    _.id, onDelete=ForeignKeyAction.Cascade
  )

  def * = (id.?, public_id, taking_id, category, amount, currency, type_of_source, type_location.?, type_contact_person.?, type_email.?, type_address.?, receipt.?, norms) <> (SourceReader.apply, SourceReader.unapply)

  def pk = primaryKey("primaryKey", id)

  def sortBy(sort: Sort) = {
    sort.field match {
      case "taking_id" => Some(sort.dir match {
        case Descending => this.taking_id.desc.nullsFirst
        case Ascending => this.taking_id.asc.nullsFirst
        case _ => this.taking_id.asc.nullsFirst
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
