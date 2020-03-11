package daos.reader

import java.util.UUID
import models.frontend.InvolvedCrew
import slick.jdbc.GetResult
import org.checkerframework.framework.qual.FieldInvariant

case class InvolvedCrewReader(
  id: Long,
  taking_id: Long,
  crew_id: UUID,
  name: String
  ) {
    def toInvolvedCrew: InvolvedCrew = InvolvedCrew(crew_id, name)
  }

object InvolvedCrewReader extends ((Long, Long, UUID, String) => InvolvedCrewReader ) {

  def apply(crew: InvolvedCrew, taking_id: Long, id:Option[Long] = None): InvolvedCrewReader = 
    InvolvedCrewReader(
      id.getOrElse(0),
      taking_id,
      crew.uuid,
      crew.name
      )
  
  def apply(tuple: (Long, Long, String, String)): InvolvedCrewReader = 
    InvolvedCrewReader(tuple._1, tuple._2, UUID.fromString(tuple._3), tuple._4)

  def unapply(arg: InvolvedCrewReader): Option[(Long, Long, String, String)] =
    Some((arg.id, arg.taking_id, arg.crew_id.toString, arg.name))

  implicit val getInvolvedCrewReader = GetResult( r => 
    InvolvedCrewReader(r.nextLong, r.nextLong, UUID.fromString(r.nextString), r.nextString)
      )
}
