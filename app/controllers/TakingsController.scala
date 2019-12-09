package controllers

import java.util.UUID

import javax.inject._
import play.api._
import play.api.mvc._
import com.mohiva.play.silhouette.api.Silhouette
import org.vivaconagua.play2OauthClient.silhouette.CookieEnv
import org.vivaconagua.play2OauthClient.silhouette.UserService
import org.vivaconagua.play2OauthClient.drops.authorization._
import play.api.libs.json.{JsError, Json, Reads}
import play.api.Configuration
import models.frontend.{ Taking, TakingStub, Page, Sort, TakingQueryBody, TakingFilter}
import responses.WebAppResult
import service.TakingsService
import utils.permissions.TakingPermission
//import utils.{Ascending, TakingFilter, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TakingsController @Inject()(
                                     parser: PlayBodyParsers,
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService,
                                     service: TakingsService,
                                     permission: TakingPermission
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  implicit val ec = ExecutionContext.global

  /*case class TakingQueryBody(page: Option[Page], sort: Option[Sort], filter: Option[TakingFilter])
  object TakingQueryBody {
    implicit val takingQueryBodyFormat = Json.format[TakingQueryBody]
  }*/
  
  /** validate a given Json type A
   * @return
   */
  
  def validateJson[A: Reads] = parser.json.validate(_.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e))))
  /**
    * Reads all currently saved takings and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(validateJson[TakingQueryBody]) { implicit request => {
    service.all(request.body.page, request.body.sort, permission.restrict(request.body.filter, request.identity))
      .map(takings => Ok(Json.toJson(takings)))
  }}
  
  /**
    * Saves a given taking on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(validateJson[TakingStub]) { implicit request => {
    service.save(request.body.toTaking).map(_ match {
      case Right(databaseTaking) => WebAppResult.Ok(Json.toJson(List(databaseTaking))).toResult(request)
      case Left(exception) => WebAppResult.InternalServerError(exception).toResult(request)
    })
  }}

  /**
    * Returns the count of takings.
    *
    * @author Johann Sell
    * @return
    */
  def update = silhouette.SecuredAction((IsAdmin || IsEmployee)).async(validateJson[Taking]) {
    implicit request => {
      service.update(request.body).map(_ match {
        case Right(databaseTaking) => Ok(Json.toJson(databaseTaking))
        case Left(exception) => InternalServerError("") 
      })
    }
  }

  def count = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(validateJson[TakingQueryBody]) { implicit request => {
    service.count(permission.restrict(request.body.filter, request.identity))
      .map(count => Ok(Json.obj("count" -> count)))
    }}
}
