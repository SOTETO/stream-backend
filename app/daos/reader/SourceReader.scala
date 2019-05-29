package daos.reader

import models.frontend.Source
import slick.jdbc.GetResult

case class SourceReader(
                       id: Long,
                       donation_id: Long,
                       category: String,
                       amount: Double,
                       currency: String,
                       type_of_source: String
                       ) {
  def toSource: Source = Source(category, amount, currency, type_of_source)
}

object SourceReader extends ((Long, Long, String, Double, String, String) => SourceReader) {

  def apply(tuple: (Long, Long, String, Double, String, String)): SourceReader =
    SourceReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6)

  implicit val getSourceReader = GetResult(r =>
    SourceReader(r.nextLong, r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextString)
  )
}