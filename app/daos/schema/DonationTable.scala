package daos.schema

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
import daos.reader.DonationReader
import slick.lifted.{ColumnOrdered, Tag}
import utils.{Ascending, Descending, Sort}
import utils.DonationFilter

class DonationTable(tag: Tag) extends Table[DonationReader](tag, "Donation") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def public_id = column[String]("public_id")
  def received = column[Long]("received")
  def description = column[String]("description")
  def category = column[String]("category")
  def norms = column[String]("norms")
  def comment = column[String]("comment")
  def reason_for_payment = column[String]("reason_for_payment")
  def receipt = column[Boolean]("receipt")
  def author = column[String]("author")
  def crew = column[String]("crew")
  def created = column[Long]("created")
  def updated = column[Long]("updated")

  def * = (id, public_id, received, description, category, norms, comment.?, reason_for_payment.?, receipt.?, author, crew, updated, created) <> (DonationReader.apply, DonationReader.unapply)

  def pk = primaryKey("primaryKey", id)
  
  def filter(filter: Option[DonationFilter]) = {
    filter.map(f => {
        List(
          f.name.map(name => this.description like "%" + name +"%"),
          f.publicId.map(ids => this.public_id.inSet(ids.map(_.toString()))),
         // f.crew.map(table.crew === _.toString())
          f.norms.map(norms => this.norms === norms)
        ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true:Rep[Boolean])
    }).getOrElse(Nil)
  }
  


  def sortByDir(sort: Sort, field: Rep[_ >: String with Long with Double with Boolean]) = sort.dir match {
    case Descending => field match {
      case l: Rep[Long @unchecked] => Some(l.desc.nullsFirst)
      //case s: Rep[String @unchecked] => Some(s.desc.nullsFirst)
     // case d: Rep[Double @unchecked] => Some(d.desc.nullsFirst)
      //case b: Rep[Boolean @unchecked] => Some(b.desc.nullsFirst)
      case _ => None
    }
    case Ascending => field match {
      case l: Rep[Long  @unchecked] => Some(l.asc.nullsFirst)
      //case s: Rep[String @unchecked] => Some(s.asc.nullsFirst)
      //case d: Rep[Double @unchecked] => Some(d.asc.nullsFirst)
      //case b: Rep[Boolean @unchecked] => Some(b.asc.nullsFirst)
      case _ => None
    }
    case _ => field match {
      case l: Rep[Long @unchecked] => Some(l.asc.nullsFirst)
      //case s: Rep[String @unchecked] => Some(s.asc.nullsFirst)
      //case d: Rep[Double @unchecked] => Some(d.asc.nullsFirst)
      //case b: Rep[Boolean @unchecked] => Some(b.asc.nullsFirst)
      case _ => None
    }
  }

  def sortBy(sort: Sort) = {
    sortByField(sort).flatMap(sortByDir(sort, _))
  }
  
  def sortByField(sort: Sort) = {
    sort.field match {
      case "public_id" => Some(this.public_id)
      case "received" => Some(this.received)
      case "description" => Some(this.description)
      case "category" => Some(this.category)
      case "norms" => Some(this.norms)
      case "comment" => Some(this.comment)
      case "reason_for_payment" => Some(this.reason_for_payment)
      case "receipt" => Some(this.receipt)
      case "author" => Some(this.author)
      case "created" => Some(this.created)
      case "updated" => Some(this.updated)
      case _ => None
    }
  }
}
