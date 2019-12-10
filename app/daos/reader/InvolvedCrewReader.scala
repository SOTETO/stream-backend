package daos.reader

import java.util.UUID
import models.frontend.Taking
import slick.jdbc.GetResult
import org.checkerframework.framework.qual.FieldInvariant

case class InvolvedCrewReader(
  id: Option[Long],
  taking_id: Long,
  crew_id: UUID,
  name: String
  )

object InvolvedCrewReader extends ((Option[Long], Long, UUID, String) => InvolvedCrewReader ) {
  
  def apply(tuple: (Option[Long], Long, String, String)): InvolvedCrewReader = 
    InvolvedCrewReader(tuple._1, tuple._2, UUID.fromString(tuple._3), tuple._4)

  implicit val getInvolvedCrewReader = GetResult( r => 
    InvolvedCrewReader(r.nextLongOption, r.nextLong, UUID.fromString(r.nextString), r.nextString)
      )
}
