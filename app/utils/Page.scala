package utils

import play.api.libs.json.Json

/**
  * Description of a page of a set of business objects.
  *
  * @author Johann Sell
  * @param size how many business objects a page contains
  * @param offset how many business objects are omitted
  * @param number of the page
  */
case class Page(size: Int, offset: Int, number: Option[Int] = None)

object Page {
  implicit val pageFormat = Json.format[Page]
}
