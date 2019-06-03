package controllers


import java.util.UUID
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import org.vivaconagua.play2OauthClient.silhouette.{CookieEnv, UserService}
import play.api.mvc.{AbstractController, ControllerComponents, BodyParsers}
import models.frontend.Deposit
import play.api.Configuration
import play.api.libs.json.{Json, Reads, JsError}
import responses.WebAppResult

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws._
import services.DepositService
import utils.{Page, Sort}
//import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class DepositController @Inject() (
  config: Configuration,
  cc: ControllerComponents,
  silhouette: Silhouette[CookieEnv],
  userService: UserService,
  service: DepositService,
  implicit val ec: ExecutionContext
) extends AbstractController(cc) {
  
  /* validate a given Json
   * if the Json is valid, the function return the request
   * else return BadRequest contains the JsError of the validation process
   */
  def validateJson[A: Reads] = BodyParsers.parse.json.validate(_.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e))))
  
  // query body for sorting 
  case class QueryBody(page: Page, sort: Sort)
  object QueryBody {
      implicit val queryBodyFormat = Json.format[QueryBody]
  }

  /**
   * All action controller return the Deposit Model as Json or an simple http error 
   */
  def create = silhouette.SecuredAction(validateJson[Deposit]).async { implicit request => {
    service.create(request.body).map(result => result match {
      case Some(deposit) => Ok(Json.toJson(deposit))
      case _ => BadRequest("TODO: create error")
    })
  }}

  def update = silhouette.SecuredAction(validateJson[Deposit]).async { implicit request => {
    service.update(request.body).map(result => result match {
      case Some(deposit) => Ok(Json.toJson(deposit))
      case _ => BadRequest("TODO: create error")
    })
  }}

  // return 200 if delete is successful 
  def delete(id: UUID) = silhouette.SecuredAction.async { implicit request => {
    service.delete(id).map(result => result match {
      case true => Ok("TODO: delete message")
      case false => BadRequest("TODO: delete error")
    })
  }}

  def all = silhouette.SecuredAction(validateJson[QueryBody]).async { implicit request => {
    service.all(request.body.page, request.body.sort, request.body.filter).map(result => result match {
      case Some(list) => Ok(Json.toJson(list))
      case _ => BadRequest("TODO: all error")
    })
  }}
  
  def count = silhouette.SecuredAction(validateJson[QueryBody]).async { implicit request => {
    service.count(request.body.filter).map(result => result match {
      case Some(list) => Ok(Json.obj("count" -> list ))
      case _ => BadRequest("TODO: count error")
    })
  }}
}
