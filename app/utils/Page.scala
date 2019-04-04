package utils

import play.api.libs.json.Json

case class Page(size: Int, offset: Int, number: Option[Int] = None)

object Page {
  implicit val pageFormat = Json.format[Page]
}
