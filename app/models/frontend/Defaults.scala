package models.frontend

import java.util.UUID
import play.api.libs.json.{Json, JsPath, Reads, Writes}
import play.api.libs.functional.syntax._

/**
  * Description of a page of a set of business objects.
  *
  * @author Johann Sell
  * @param size how many business objects a page contains
  * @param offset how many business objects are omitted
  * @param number of the page
  */
case class Page(size: Int, offset: Int, number: Option[Int] = None)
/** Factory for [[Page]] instance*/
object Page {
  implicit val pageFormat = Json.format[Page]
}

/**
 * A Json used to confirm a business object
 * @param id public_id of an user
 * @param data date formated in utc
 */
case class ConfirmBody(id: UUID, date: Long)
/** Factory for [[ConfirmBody]] instance*/
object ConfirmBody {
    implicit val confirmBodyFormat = Json.format[ConfirmBody]
  }

/**
  * Describes a sorting direction.
  *
  * @author Johann Sell
  * @param name used for communication with other systems (database or frontend)
  */
sealed abstract class SortDir(val name: String) {

  /**
    * Sorting directions can be compared by another sorting direction or a another string that is used to communicate
    * with other systems.
    *
    * @author Johann Sell
    * @param o Any
    * @return Boolean
    */
  override def equals(o: scala.Any): Boolean = o match {
    case d: SortDir => this.name == d.name
    case d: String => this.name == d
    case _ => false
  }
}

/**
  * Ascending is one possible sorting direction.
  * @author Johann Sell
  */
case object Ascending extends SortDir("ASC")
/**
  * Descending is one possible sorting direction.
  * @author Johann Sell
  */
case object Descending extends SortDir("DESC")

/**
  * Companion object for {{{SortDir}}}
  *
  * @author Johann Sell
  */
object SortDir {
  /**
    * Instanciate an optional sorting direction by a given string. Reduces the possible instances to a set of known ones.
    *
    * @author Johann Sell
    * @param dir String
    * @return {{{Option[String]}}}
    */
  def apply(dir: String) : Option[SortDir] = List(Ascending, Descending).find(_ == dir)

  implicit val sortDirReads: Reads[SortDir] =
    JsPath.read[String].map(SortDir( _ ).getOrElse(Ascending))

  implicit val sortDirWrites: Writes[SortDir] =
    JsPath.write[String].contramap[SortDir](dir => dir.name)
}

/**
  * A complete description of a sorting criteria of requests of sets of business objects.
  *
  * @author Johann Sell
  * @param fieldDescription describes the attribute that has to be used as a sorting criteria.
  * @param dir describes the direction of sorting
  */
case class Sort(private val fieldDescription: String, dir: SortDir) {
  /**
    * Returns the field
    *
    * @author Johann Sell
    * @return
    */
  def field: String = fieldDescription.split('.').last

  /**
    * Returns a referenced model, if given.
    *
    * @author Johann Sell
    * @return
    */
  def model: Option[String] = fieldDescription.split('.') match {
    case array if array.length > 1 => array.headOption
    case _ => None
  }

  /**
    * Checks, if a model has been referenced
    *
    * @author Johann Sell
    * @return
    */
  def hasModel: Boolean = model.isDefined
}

object Sort {
  implicit val sortReads: Reads[Sort] = (
    (JsPath \ "field").read[String] and
      (JsPath \ "dir").read[SortDir]
    )(Sort.apply _)

  implicit val sortWrites: Writes[Sort] = (
    (JsPath \ "field").write[String] and
      (JsPath \ "dir").write[SortDir]
  )(unlift(Sort.unapply))
}

case class InvolvedCrew(uuid: UUID, name: String)

object InvolvedCrew {
  implicit val involvedCrewFormat = Json.format[InvolvedCrew]
}

case class Confirmed(date: Long, user: InvolvedSupporter)

object Confirmed {
  implicit val confirmedFormat = Json.format[Confirmed]
}
