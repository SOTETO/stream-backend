package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.DonationReader
import slick.lifted.Tag

class DonationTable(tag: Tag) extends Table[DonationReader](tag, "Donation") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def public_id = column[String]("public_id")
  def received = column[Long]("received")
  def description = column[String]("description")
  def category = column[String]("category")
  def comment = column[String]("comment")
  def reason_for_payment = column[String]("reason_for_payment")
  def receipt = column[Boolean]("receipt")
  def author = column[String]("author")
  def created = column[Long]("created")
  def updated = column[Long]("updated")

  def * = (id, public_id, received, description, category, comment.?, reason_for_payment.?, receipt.?, author, updated, created) <> (DonationReader.apply, DonationReader.unapply)

  def pk = primaryKey("primaryKey", id)
}