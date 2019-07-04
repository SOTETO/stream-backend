package daos.schema

import slick.jdbc.MySQLProfile.api._
import daos.reader.{HouseholdReader, HouseholdVersionReader, PlaceMessageReader}
import slick.lifted.Tag
import utils.{Ascending, Descending, Sort}
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

  def sortByField(sort: Sort) = {
    sort.field match {
      case "public_id" => Some(this.publicId)
      case "iban" => Some(this.iban)
      case "bic" => Some(this.bic)
      case "created" => Some(this.created)
      case "updated" => Some(this.updated)
      case "author" => Some(this.author)
      case "editor" => Some(this.editor)
      case "amount" => Some(this.amount)
      case "currency" => Some(this.currency)
      case "reason_what" => Some(this.reasonWhat)
      case "reason_wherefor" => Some(this.reasonWherefor)
      case "request" => Some(this.request)
      case "volunteer_manager" => Some(this.volunteerManager)
      case "employee" => Some(this.employee)
      case _ => None
    }
  }

  def sortByDir(sort: Sort, field: Rep[_ >: String with Long with Double with Boolean]) = sort.dir match {
    case Descending => field match {
      case l: Rep[Long] => Some(l.desc.nullsFirst)
      case s: Rep[String] => Some(s.desc.nullsFirst)
      case d: Rep[Double] => Some(d.desc.nullsFirst)
      case b: Rep[Boolean] => Some(b.desc.nullsFirst)
      case _ => None
    }
    case Ascending => field match {
      case l: Rep[Long] => Some(l.asc.nullsFirst)
      case s: Rep[String] => Some(s.asc.nullsFirst)
      case d: Rep[Double] => Some(d.asc.nullsFirst)
      case b: Rep[Boolean] => Some(b.asc.nullsFirst)
      case _ => None
    }
    case _ => field match {
      case l: Rep[Long] => Some(l.asc.nullsFirst)
      case s: Rep[String] => Some(s.asc.nullsFirst)
      case d: Rep[Double] => Some(d.asc.nullsFirst)
      case b: Rep[Boolean] => Some(b.asc.nullsFirst)
      case _ => None
    }
  }

  def sortBy(sort: Sort) = {
    sortByField(sort).flatMap(sortByDir(sort, _))
  }
}

class PlaceMessageTable(tag: Tag) extends Table[PlaceMessageReader](tag, "Place_Message") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def token = column[Int]("token")
  def householdId = column[Long]("household_id")
  
  def * = (id, name, token, householdId) <> (PlaceMessageReader.apply, PlaceMessageReader.unapply)
  
  def householdKey = foreignKey("household_id", householdId, TableQuery[HouseholdTable])(_.id, onUpdate = ForeignKeyAction.Cascade)

  def sortBy(sort: Sort) = {
    sort.field match {
      case "name" => Some(sort.dir match {
        case Descending => this.name.desc.nullsFirst
        case Ascending => this.name.asc.nullsFirst
        case _ => this.name.asc.nullsFirst
      })
      case "token" => Some(sort.dir match {
        case Descending => this.token.desc.nullsFirst
        case Ascending => this.token.asc.nullsFirst
        case _ => this.token.asc.nullsFirst
      })
      case _ => None
    }
  }
}

class HouseholdTable(tag: Tag) extends Table[HouseholdReader](tag, "Household") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def publicId = column[String]("public_id")
  
  def * = (id, publicId) <> (HouseholdReader.apply, HouseholdReader.unapply)
} 
