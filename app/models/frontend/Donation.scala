package models.frontend

import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Details(reasonForPayment: String, receipt: Boolean) // Todo: Optional Partner

case class Context(description: String, category: String)

case class Source(category: String, amount: Double, formatted: String, typeOfSource: String)

case class Amount(received: Long, involvedSupporter: List[UUID], sources: List[Source])

case class Donation(
                   id: UUID,
                   amount: Amount,
                   context: Context,
                   comment: Option[String],
                   details: Option[Details],
                   author: UUID,
                   created: Long,
                   updated: Long
                   )

object Details {
  implicit val detailsFormat = Json.format[Details]
}

object Context {
  implicit val contextFormat = Json.format[Context]
}

object Source {
  implicit val sourceWrites: Writes[Source] = (
    (JsPath \ "category").write[String] and
      (JsPath \ "amount").write[Double] and
      (JsPath \ "formatted").write[String] and
      (JsPath \ "type").write[String]
    )(unlift(Source.unapply))

  implicit val sourceReads: Reads[Source] = (
    (JsPath \ "category").read[String] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "formatted").read[String] and
      (JsPath \ "type").read[String]
    )(Source.apply _)
}

object Amount {
  implicit val amountFormat = Json.format[Amount]
}

object Donation {
  implicit val donationFormat = Json.format[Donation]
}