package controllers.responses

import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Success(msg: String, i18n: String, data: JsValue)

object Success {
  def apply(data: JsValue, i18n: String = "webapp.result.success")(implicit messagesProvider: MessagesProvider) : Success =
    Success(Messages(i18n), i18n, data)

  def apply(t: (String, String, JsValue)) : Success =
    Success(t._1, t._2, t._3)

  implicit val successDonationWrites: Writes[Success] = (
    (JsPath \ "msg").write[String] and
      (JsPath \ "i18n").write[String] and
      (JsPath \ "data").write[JsValue]
    )(unlift(Success.unapply))

  implicit val successDonationReads: Reads[Success] = (
    (JsPath \ "msg").read[String] and
      (JsPath \ "i18n").read[String] and
      (JsPath \ "data").read[JsValue]
    )((msg, i18n, data) => Success(msg, i18n, data))
//  implicit def successFormatter[T] = { implicit formatter : play.api.libs.json.Format[T] =>
//    Json.format[Success[T]]
//  }
}