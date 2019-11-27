package daos.reader

import java.util.UUID
import models.frontend.{Taking, Source, Amount}
import slick.jdbc.GetResult

case class SourceReader(
                       id: Option[Long],
                       public_id: String,
                       taking_id: Long,
                       category: String,
                       amount: Double,
                       currency: String,
                       type_of_source: String,
                       norms: String
                       ) {
  def toSource: Source = Source(Some(UUID.fromString(public_id)), category, Amount(amount, currency), type_of_source, norms)
}

object SourceReader extends ((Option[Long], String, Long, String, Double, String, String, String) => SourceReader) {
  
  def apply(source: Source, id: Long, taking_id: Long, public_id: String): SourceReader = 
    SourceReader(Some(id), public_id, taking_id, source.category, source.amount.amount, source.amount.currency, source.typeOfSource, source.norms)
  def apply(source: Source, taking_id: Long): SourceReader =
    SourceReader(None, UUID.randomUUID().toString, taking_id, source.category, source.amount.amount, source.amount.currency, source.typeOfSource, source.norms)

  def apply(tuple: (Option[Long], String, Long, String, Double, String, String, String)): SourceReader =
    SourceReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8)

  implicit val getSourceReader = GetResult(r =>
    SourceReader(r.nextLongOption, r.nextString, r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextString, r.nextString())
  )
}
