package daos.schema

import slick.jdbc.MySQLProfile.api._
import daos.reader.{HouseholdReader, HouseholdVersionReader, PlaceMessageReader}
import slick.lifted.Tag
/**
 * Implements the Database Schema of Household
 *
 * -------------         --------------------
 * | Household | 1 <-- n | HouseholdVersion |
 * -------------         --------------------
 *        1
 *        ^
 *        |
 *        n
 * ----------------
 * | PlaceMessage |
 * ----------------
 * 
 *
 */



class HouseholdVersionTable(tag: Tag) extends Table[HouseholdVersionReader](tag, "Household_Version") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("public_id")
  def iban = column[String]("iban")
  def bic = column[String]("bic")
  def created = column[Long]("created")
  def updated = column[Long]("updated")
  def author = column[String]("author")
  def editor = column[String]("editor")
  def amount = column[Double]("amount")
  def currency = column[String]("currency")
  def reasonWhat = column[String]("reason_what")
  def reasonWherefor = column[String]("reason_wherefor")
  def request = column[Boolean]("request")
  def volunteerManager = column[String]("volunteer_manager")
  def employee = column[String]("employee")
  def householdId = column[Long]("household_id")

  def * = (id, publicId, iban.?, bic.?, created, updated, author.?, editor.?, amount, currency, reasonWhat.?, reasonWherefor.?, request, volunteerManager.?, employee.?, householdId) <> (HouseholdVersionReader.apply, HouseholdVersionReader.unapply)

  def householdKey = foreignKey("household_id", householdId, TableQuery[HouseholdTable])(_.id, onUpdate = ForeignKeyAction.Cascade)
}

class PlaceMessageTable(tag: Tag) extends Table[PlaceMessageReader](tag, "Place_Message") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def token = column[Int]("token")
  def householdId = column[Long]("household_id")
  
  def * = (id, name, token, householdId) <> (PlaceMessageReader.apply, PlaceMessageReader.unapply)
  
  def householdKey = foreignKey("household_id", householdId, TableQuery[HouseholdTable])(_.id, onUpdate = ForeignKeyAction.Cascade)
}

class HouseholdTable(tag: Tag) extends Table[HouseholdReader](tag, "Household") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("public_id")
  
  def * = (id, publicId) <> (HouseholdReader.apply, HouseholdReader.unapply)
} 