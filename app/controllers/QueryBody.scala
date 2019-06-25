package controllers

import play.api.libs.json.Json
import utils.{HouseholdFilter, Page, Sort}

case class QueryBody(page: Page, sort: Sort, filter: Option[HouseholdFilter])

object QueryBody {
  implicit val queryBodyFormat = Json.format[QueryBody]
}
