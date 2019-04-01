package controllers

import com.mohiva.play.silhouette.api.Silhouette
import controllers.restQuery.values.RESTFilter
import javax.inject._
import org.vivaconagua.play2OauthClient.silhouette.{CookieEnv, UserService}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.frontend.Household
import play.api.Configuration
import play.api.libs.json.Json
import responses.WebAppResult

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws._
import services.HouseholdService

@Singleton
class HouseholdController @Inject()(
                                     implicit ws: WSClient,
                                     config: Configuration,
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService,
                                     service: HouseholdService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {
//  var household : Future[List[Household]] = Household.initTestData(20, config)

  /**
    * Reads all currently saved donations and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction.async { implicit request =>
    implicit val ec = ExecutionContext.global
    service.all.map(list => WebAppResult.Ok(Json.toJson( list )).toResult(request))
  }

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
        service.save(household).map(_.toList).map(list =>
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
        service.update(household).map(h =>
          WebAppResult.Ok(Json.toJson(h.toList)).toResult(request)
        )
      }
    )
  }

  def restService = Action.async(parse.json) { request =>
    request.body.validate[RESTFilter](Household.filterJson).fold(
      errors => Future.successful(BadRequest(errors.map(e => e._1 + " :: " + e._2).mkString("\n"))),
      filter => Future.successful(Ok(Json.toJson(filter)))
    )
  }
}