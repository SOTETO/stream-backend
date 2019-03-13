package controllers.responses

import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.json.{JsPath, JsValue, Json, JsonValidationError}
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Status

case class WebAppResult(result: play.api.mvc.Results.Status, body: Either[Success, Failure]) {
  def toResult : RequestHeader => Result = { implicit request: RequestHeader =>
    request.accepts("application/json") match {
      case true => result(body.fold(
        success => Json.toJson(success),
        failure => Json.toJson(failure)
      )).as("application/json")
      case _ => result(body.fold(
        success => success.msg,
        failure => failure.msg
      ))
    }
  }
}

object WebAppResult {
  def Ok(data: JsValue)(implicit messagesProvider: MessagesProvider) = WebAppResult(play.api.mvc.Results.Ok, Left(Success(data)))

  def BadRequest(errors: Seq[(JsPath, Seq[JsonValidationError])], i18n: String = "a")(implicit messagesProvider: MessagesProvider) = WebAppResult(
    play.api.mvc.Results.BadRequest,
    Right(Failure(Messages(i18n), i18n, errors.map(error =>
      error._1.toJsonString -> Json.toJson(error._2.map(_.messages))
    ).toMap))
  )
}