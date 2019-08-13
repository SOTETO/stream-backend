package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.TakingsReader
import slick.lifted.{ColumnOrdered, Tag}
import utils.{Ascending, Descending, Sort}

class TakingsTable(tag: Tag) extends Table[TakingsReader](tag, "Takings"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def public_id = column[String]("public_id")
  def received = column[String]("received")
  def description = column[String]("description")
  def category = column[String]("category")
  def comment = column[String]("comment")
  def reason_for_payment = column[String]("reason_for_payment")
  def receipt = column[String]("receipt")
  def author = column[String]("author")
  def crew = column[String]("crew")
  def created = column[Long]("created")
  def updated = column[Long]("updated")
  
  def * (id, public_id, received, description, category, comment, reason_for_payment, receipt, author, crew, created, updated) <> (TakingsReader.apply, TakingsReader.unapply)

  def pk = primaryKey("primaryKey", id)

}
