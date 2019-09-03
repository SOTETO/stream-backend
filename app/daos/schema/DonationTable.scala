package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.DonationReader
import slick.lifted.{ColumnOrdered, Tag}
import utils.{Ascending, Descending, Sort}

class DonationTable(tag: Tag) extends Table[DonationReader](tag, "Donation") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def public_id = column[String]("public_id")
  def received = column[Long]("received")
  def description = column[String]("description")
  def category = column[String]("category")
  def norms = column[String]("norms")
  def comment = column[String]("comment")
  def reason_for_payment = column[String]("reason_for_payment")
  def receipt = column[Boolean]("receipt")
  def author = column[String]("author")
  def crew = column[String]("crew")
  def created = column[Long]("created")
  def updated = column[Long]("updated")

  def * = (id, public_id, received, description, category, norms, comment.?, reason_for_payment.?, receipt.?, author, crew, updated, created) <> (DonationReader.apply, DonationReader.unapply)

  def pk = primaryKey("primaryKey", id)

  def sortBy(sort: Sort) = {
    sort.field match {
      case "public_id" => Some(sort.dir match {
        case Descending => this.public_id.desc.nullsFirst
        case Ascending => this.public_id.asc.nullsFirst
        case _ => this.public_id.asc.nullsFirst
      })
      case "received" => Some(sort.dir match {
        case Descending => this.received.desc.nullsFirst
        case Ascending => this.received.asc.nullsFirst
        case _ => this.received.asc.nullsFirst
      })
      case "description" => Some(sort.dir match {
        case Descending => this.description.desc.nullsFirst
        case Ascending => this.description.asc.nullsFirst
        case _ => this.description.asc.nullsFirst
      })
      case "category" => Some(sort.dir match {
        case Descending => this.category.desc.nullsFirst
        case Ascending => this.category.asc.nullsFirst
        case _ => this.category.asc.nullsFirst
      })
      case "norms" => Some(sort.dir match {
        case Descending => this.norms.desc.nullsFirst
        case Ascending => this.norms.asc.nullsFirst
        case _ => this.norms.asc.nullsFirst
      })
      case "comment" => Some(sort.dir match {
        case Descending => this.comment.desc.nullsFirst
        case Ascending => this.comment.asc.nullsFirst
        case _ => this.comment.asc.nullsFirst
      })
      case "reason_for_payment" => Some(sort.dir match {
        case Descending => this.reason_for_payment.desc.nullsFirst
        case Ascending => this.reason_for_payment.asc.nullsFirst
        case _ => this.reason_for_payment.asc.nullsFirst
      })
      case "receipt" => Some(sort.dir match {
        case Descending => this.receipt.desc.nullsFirst
        case Ascending => this.receipt.asc.nullsFirst
        case _ => this.receipt.asc.nullsFirst
      })
      case "author" => Some(sort.dir match {
        case Descending => this.author.desc.nullsFirst
        case Ascending => this.author.asc.nullsFirst
        case _ => this.author.asc.nullsFirst
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
