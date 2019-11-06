package daos.schema

import daos.reader.InvolvedSupporterReader
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

class InvolvedSupporterTable(tag: Tag) extends Table[InvolvedSupporterReader](tag, "InvolvedSupporter")  {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def taking_id = column[Long]("taking_id")
  def supporter_id = column[String]("supporter_id")

  val takingTable = TableQuery[TakingTable]

  def takings = foreignKey("FK_Sup_Taking", taking_id, takingTable)(
    _.id, onDelete=ForeignKeyAction.Cascade
  )

  def * = (id.?, taking_id, supporter_id) <> (InvolvedSupporterReader.apply, InvolvedSupporterReader.unapply)

  def pk = primaryKey("primaryKey", id)
}
