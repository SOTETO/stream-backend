package daos.schema

import slick.jdbc.MySQLProfile.api._
import daos.reader.PlaceMessageReader
import slick.lifted.{ColumnOrdered, Tag}

class PlaceMessageTable(tag: Tag) extends Table[PlaceMessageReader](tag, "Place_Message") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def token = column[Int]("token")
  def name = column[String]("name")
  
  def * = (id, token, name) <> (PlaceMessageReader.apply, PlaceMessageReader.unapply)

}
