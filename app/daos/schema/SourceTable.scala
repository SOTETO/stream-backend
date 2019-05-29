package daos.schema

import daos.reader.SourceReader
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

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

  def * = (id, donation_id, category, amount, currency, type_of_source) <> (SourceReader.apply, SourceReader.unapply)

  def pk = primaryKey("primaryKey", id)
}
