package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.ConfirmedReader
import slick.lifted.{ColumnOrdered, Tag}


class ConfirmedTable(tag: Tag) extends Table[ConfirmedReader](tag, "Confirmed") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userUUID = column[String]("user_uuid")
  def userName = column[String]("user_name")
  def dateOfConfirm = column[Long]("date_of_confirm")
  def depositId = column[Long]("Deposit_id")

  def * = (id, userUUID, userName, dateOfConfirm, depositId) <> (ConfirmedReader.tupled, ConfirmedReader.unapply)

  def depositKey = foreignKey("Deposit_id", depositId, TableQuery[DepositTable])(_.id, onUpdate= ForeignKeyAction.Cascade)
}
