package utils

import daos.schema.DonationTable
import play.api.libs.json.Json
import slick.lifted.TableQuery

case class DonationFilter(
                         name: Option[String]
                         )

object DonationFilter {
  implicit val donationFilterFormat = Json.format[DonationFilter]
}