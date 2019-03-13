package controllers.responses

import play.api.libs.json.{JsValue, Json}

case class Failure(msg: String, i18n: String, infos: Map[String, JsValue])

object Failure {
  implicit val failureFormat = Json.format[Failure]
}