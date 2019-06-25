package utils

import java.util.UUID

import daos.schema.DonationTable
import play.api.libs.json.Json
import slick.lifted.TableQuery

case class DonationFilter(
                         publicId: Option[Set[UUID]], // content of the set has to be concatenated by OR
                         name: Option[String]
                         )

object DonationFilter {
  implicit val donationFilterFormat = Json.format[DonationFilter]
}