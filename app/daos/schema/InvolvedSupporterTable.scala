package daos.schema

import daos.reader.InvolvedSupporterReader
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

class InvolvedSupporterTable(tag: Tag) extends Table[InvolvedSupporterReader](tag, "InvolvedSupporter")  {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def donation_id = column[Long]("donation_id")
  def supporter_id = column[String]("supporter_id")

  val donationTable = TableQuery[DonationTable]

  def donations = foreignKey("FK_Sup_Donation", donation_id, donationTable)(
    _.id, onDelete=ForeignKeyAction.Cascade
  )

  def * = (id, donation_id, supporter_id) <> (InvolvedSupporterReader.apply, InvolvedSupporterReader.unapply)

  def pk = primaryKey("primaryKey", id)
}
