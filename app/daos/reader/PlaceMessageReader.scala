package daos.reader

import models.frontend._
import slick.jdbc.GetResult

case class PlaceMessageReader(
  id: Long,
  token: Int,
  name: String,
  )
object PlaceMessageReader extends ((Long, Int, String) => PlaceMessageReader) {
  
  def apply(tuple: (Long, Int, String)): PlaceMessageReader =
    PlaceMessageReader( tuple._1, tuple._2, tuple._3)
  
}
  
