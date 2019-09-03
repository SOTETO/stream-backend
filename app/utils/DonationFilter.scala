package utils

import java.util.UUID

import daos.schema.DonationTable
import play.api.libs.json.Json
import slick.lifted.TableQuery

case class DonationFilter(
                         publicId: Option[Set[UUID]], // content of the set has to be concatenated by OR
                         crew: Option[Set[UUID]],
                         name: Option[String],
                         norms: Option[String]
                         ) {
  def + (filter: Option[DonationFilter]) : DonationFilter =
    DonationFilter(
      publicId match {
        case Some(ids) => filter.flatMap(_.publicId) match {
          case Some(otherIDs) => Some(ids ++ otherIDs)
          case None => Some(ids)
        }
        case None => filter.flatMap(_.publicId)
      },
      crew match {
        case Some(ids) => filter.flatMap(_.crew) match {
          case Some(otherIDs) => Some(ids ++ otherIDs)
          case None => Some(ids)
        }
        case None => filter.flatMap(_.crew)
      },
      name, // TODO: Do something with other name
      norms
    )
}

object DonationFilter {
  implicit val donationFilterFormat = Json.format[DonationFilter]
}
