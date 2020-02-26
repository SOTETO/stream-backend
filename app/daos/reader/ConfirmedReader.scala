package daos.reader

import models.frontend.{Confirmed, InvolvedSupporter}
import java.util.UUID
import slick.jdbc.GetResult

case class ConfirmedReader( id: Long, userUUID: String, userName: String, dateOfConfirm: Long, depositId: Long) {
  def toConfirmed : Confirmed = {
    Confirmed(dateOfConfirm, InvolvedSupporter(UUID.fromString(userUUID), userName))
  }
}

object ConfirmedReader extends ((Long, String, String, Long, Long) => ConfirmedReader) {
  implicit  val getConfimedReader = GetResult(r => 
       ConfirmedReader(r.nextLong, r.nextString, r.nextString, r.nextLong, r.nextLong))
}
