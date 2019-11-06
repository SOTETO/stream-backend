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
import models.frontend.Taking
import responses.WebAppResult
import service.TakingsService
import utils.{Ascending, TakingFilter, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TakingsController @Inject()(
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService,
                                     service: TakingsService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  implicit val ec = ExecutionContext.global

  case class TakingQueryBody(page: Option[Page], sort: Option[Sort], filter: Option[TakingFilter])
  object TakingQueryBody {
    implicit val takingQueryBodyFormat = Json.format[TakingQueryBody]
  }

  /**
    * Reads all currently saved takings and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    // Prefilter results by the users crew, if the user is a volunteer manager and no employee
    val crewFilter : Option[TakingFilter] = request.identity.isOnlyVolunteer match {
      case true => request.identity.getCrew.map((crewID) => TakingFilter(None, Some(Set(crewID)), None, None))
      case false => None
    }
    request.body.validate[TakingQueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => {
        val filter = query.filter match {
          case Some(f) => Some(f + crewFilter)
          case None => crewFilter
        }
        service.all(query.page, query.sort, filter).map(takings =>
          WebAppResult.Ok(Json.toJson(takings)).toResult(request)
        )
      }
    )
  }}

  /**
    * Saves a given taking on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    request.body.validate[Taking].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      taking => {
        service.save(taking).map(_ match {
          case Right(databaseTaking) => WebAppResult.Ok(Json.toJson(List(databaseTaking))).toResult(request)
          case Left(exception) => WebAppResult.InternalServerError(exception).toResult(request)
        })
      }
    )
  }}

  /**
    * Returns the count of takings.
    *
    * @author Johann Sell
    * @return
    */
  def count = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async(parse.json) { implicit request => {
    // Prefilter results by the users crew, if the user is a volunteer manager and no employee
    val crewFilter : Option[TakingFilter] = request.identity.isOnlyVolunteer match {
      case true => request.identity.getCrew.map(crewId => TakingFilter(None, Some(Set(crewId)), None, None))
      case false => None
    }
    request.body.validate[TakingQueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => {

        val filter = query.filter match {
          case Some(f) => Some(f + crewFilter)
          case None => crewFilter
        }
        service.count(filter).map(count =>
          WebAppResult.Ok(Json.obj("count" -> count )).toResult(request)
        )
      }
    )
  }}
}
