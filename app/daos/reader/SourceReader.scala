package daos.reader

import models.frontend.{Donation, Source}
import slick.jdbc.GetResult

case class SourceReader(
                       id: Option[Long],
                       donation_id: Long,
                       category: String,
                       amount: Double,
                       currency: String,
                       type_of_source: String
                       ) {
  def toSource: Source = Source(category, amount, currency, type_of_source)
}

object SourceReader extends ((Option[Long], Long, String, Double, String, String) => SourceReader) {

  def apply(source: Source, donation_id: Long): SourceReader =
    SourceReader(None, donation_id, source.category, source.amount, source.currency, source.typeOfSource)

  def apply(tuple: (Option[Long], Long, String, Double, String, String)): SourceReader =
    SourceReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6)

  implicit val getSourceReader = GetResult(r =>
    SourceReader(r.nextLongOption, r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextString)
  )
}