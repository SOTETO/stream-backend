package daos.schema

import daos.reader.InvolvedCrewReader
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

class InvolvedCrewTable(tag: Tag) extends Table[InvolvedCrewReader](tag, "InvolvedCrew") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def taking_id = column[Long]("taking_id")
  def crew_id = column[String]("crew_id")
  def name = column[String]("name")
  def takingTable = TableQuery[TakingTable]
  def takings = foreignKey("FK_Crew_Taking", taking_id, takingTable)(_.id, onDelete=ForeignKeyAction.Cascade)
  def * = (id, taking_id, crew_id, name) <> (InvolvedCrewReader.apply, InvolvedCrewReader.unapply)
  def pk = primaryKey("primaryKey", id)
}
