package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import org.vivaconagua.play2OauthClient.silhouette.{CookieEnv, UserService}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.frontend.{ActionMessage, ActionMessageNotExists, Household, PetriNetHouseholdState}
import play.api.Configuration
import play.api.libs.json.Json
import responses.WebAppResult

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws._
import services.HouseholdService
import utils.{HouseholdFilter, Page, Sort}

@Singleton
class HouseholdController @Inject()(
                                     implicit ws: WSClient,
                                     config: Configuration,
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService,
                                     service: HouseholdService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  /**
    * Saves a given donation on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(parse.json).async { implicit request => {
    implicit val ec = ExecutionContext.global
    request.body.validate[Household].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      household => {
        service.save(
          household.copy(state = PetriNetHouseholdState(household.versions.headOption)),
          request.identity.uuid
        ).map(_.toList).map(list =>
          WebAppResult.Ok(Json.toJson( list )).toResult(request)
        )
      }
    )
  }}

  def update = silhouette.SecuredAction(parse.json).async { implicit request =>
    implicit val ec = ExecutionContext.global
    request.body.validate[Household].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      household => {
        service.update(household, request.identity.uuid).map(h =>
          WebAppResult.Ok(Json.toJson(h.toList)).toResult(request)
        )
      }
    )
  }

  def read = silhouette.SecuredAction(parse.json).async { implicit request =>
    implicit val ec = ExecutionContext.global
    request.body.validate[QueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => service.all(query.page, query.sort, query.filter).map(list => WebAppResult.Ok(Json.toJson( list )).toResult(request))
    )
  }

  def count = silhouette.SecuredAction(parse.json).async { implicit request =>
    implicit val ec = ExecutionContext.global
    request.body.validate[QueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => service.count(query.filter).map(i => WebAppResult.Ok(Json.obj("count" -> i )).toResult(request))
    )
  }
  def stateUpdate(uuid: String, role: String) = silhouette.SecuredAction(parse.json).async { implicit request =>
    implicit val ec = ExecutionContext.global
    request.body.validate[List[ActionMessage]].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      actionMsg => service.stateUpdate(actionMsg, UUID.fromString(uuid), request.identity.uuid, role)
        .map(_.fold(
          error => WebAppResult.InternalServerError(error).toResult(request),
          household => WebAppResult.Ok(Json.toJson(household)).toResult(request)
        ))
    )
  }
}
