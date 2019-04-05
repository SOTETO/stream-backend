package utils

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed abstract class SortDir(val name: String) {
  override def equals(o: scala.Any): Boolean = o match {
    case d: SortDir => this.name == d.name
    case d: String => this.name == d
    case _ => false
  }
}

case object Ascending extends SortDir("ASC")
case object Descending extends SortDir("DESC")

object SortDir {
  def apply(dir: String) : Option[SortDir] = List(Ascending, Descending).find(_ == dir)

  implicit val sortDirReads: Reads[SortDir] =
    JsPath.read[String].map(SortDir( _ ).getOrElse(Ascending))

  implicit val sortDirWrites: Writes[SortDir] =
    JsPath.write[String].contramap[SortDir](dir => dir.name)
}

case class Sort(field: String, dir: SortDir)

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
