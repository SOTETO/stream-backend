package utils

import java.util.UUID
import play.api.libs.json.Json

/** A class for filter 
 * @constructor Create a new DepositFilter with `publicId`, `takingsId` and `crew`
 * @param publicId
 * @param takingsId
 * @param crew
 */
case class DepositFilter(
  publicId: Option[Set[UUID]], 
  takingsId: Option[Set[UUID]],
  crew: Option[Set[UUID]]
) 
{
  /** Extend a deposit filter with given crew_id
   * @param crewId public_id of a Crew as UUID
   * @return with crew extended deposit filter
   */
  def extend(crewId: UUID): DepositFilter = DepositFilter(this.publicId, this.takingsId, Some(Set(crewId)))
}

/** Factory for [[DepositFilter]] instance. Can be handle as Json */
object DepositFilter {
  implicit val depositFilterFormat = Json.format[DepositFilter]
  
  /** Creates a DepositFilter with given crewId
   *  @param crewId
   */
   
  def apply(crewId: UUID) : DepositFilter = DepositFilter(None,Some(Set(crewId)), None)
}
