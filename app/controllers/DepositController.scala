package controllers


import java.util.UUID

import play.api._
import play.api.mvc._
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import org.vivaconagua.play2OauthClient.silhouette.{CookieEnv, UserService}
import play.api.mvc.{AbstractController, BodyParsers, ControllerComponents}
import models.frontend.{Deposit , DepositStub, DepositFilter, DepositQueryBody, Page}
import org.vivaconagua.play2OauthClient.drops.authorization._
import play.api.Configuration
import play.api.libs.json.{JsError, Json, Reads}
import responses.WebAppResult

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws._
import services.DepositService
import utils.{Sort}
import utils.permissions.DepositPermission
import org.vivaconagua.play2OauthClient.silhouette.User
import play.shaded.ahc.org.asynchttpclient.request.body.Body
//import play.api.libs.concurrent.Execution.Implicits.defaultContext


/** A web-controller class for deposits.
 */
@Singleton
class DepositController @Inject() (
  parser: PlayBodyParsers,
  config: Configuration,
  cc: ControllerComponents,
  silhouette: Silhouette[CookieEnv],
  userService: UserService,
  service: DepositService,
  permission: DepositPermission,
  implicit val ec: ExecutionContext
) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  
  /** validate a given Json type A
   * @return
   */
  
  def validateJson[A: Reads] = parser.json.validate(_.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e))))



  /**
   * All action controller return the Deposit Model as Json or an simple http error 
   */
  def create = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    request.body.validate[DepositStub].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      deposit => service.create(deposit.toDeposit()).map(result => result match {
        case Right(deposit) => WebAppResult.Ok(Json.toJson(List(deposit))).toResult(request)
        case Left(exception) => WebAppResult.InternalServerError(exception).toResult(request)
      })
    )
  }}

  def update = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(validateJson[Deposit]) { implicit request => {
    service.update(request.body).map(result => result match {
      case Some(deposit) => Ok(Json.toJson(deposit))
      case _ => BadRequest("TODO: create error")
    })
  }}

  // return 200 if delete is successful 
  def delete(id: UUID) = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async { implicit request => {
    service.delete(id).map(result => result match {
      case true => Ok("TODO: delete message")
      case false => BadRequest("TODO: delete error")
    })
  }}

  def all = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    // Prefilter results by the users crew, if the user is a volunteer manager and no employee
    //val preFilter : Option[DepositFilter] = request.identity.isOnlyVolunteer match {
    //  case true => request.identity.getCrew.map(DepositFilter( _ ))
    //  case false => None
    //}
    request.body.validate[DepositQueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => {
        service.all(query.page, query.sort, permission.restrict(query.filter, request.identity)).map(list =>
          WebAppResult.Ok(Json.toJson(list)).toResult(request)
        )
      }
    )
  }}
  
  def count = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    // Prefilter results by the users crew, if the user is a volunteer manager and no employee
    //val preFilter : Option[DepositFilter] = request.identity.isOnlyVolunteer match {
    //  case true => request.identity.getCrew.map(DepositFilter( _ ))
    //  case false => None
   // }
    request.body.validate[DepositQueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => {
        service.count(
          permission.restrict(query.filter, request.identity) // add restrictions to filter
          ).map(result =>
        WebAppResult.Ok(Json.obj("count" -> result)).toResult(request)
      )})
  }}
  


  case class ConfirmBody(id: UUID, date: Long, uuid: UUID, name: String)
  object ConfirmBody {
    implicit val confirmBodyFormat = Json.format[ConfirmBody]
  }

  def confirm = silhouette.SecuredAction(
    IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    request.body.validate[ConfirmBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => service.confirm(query.id, query.date, query.uuid, query.name).map(result => WebAppResult.Ok(
        Json.obj("state" -> (result match {
          case true => "SUCCESS"
          case false => "FAILURE"
        }))
      ).toResult(request))
    )
  }
  }
}
