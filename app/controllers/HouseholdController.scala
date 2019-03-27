package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import org.vivaconagua.play2OauthClient.silhouette.{CookieEnv, UserService}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.frontend.Household
import play.api.Configuration
import play.api.libs.json.Json
import responses.WebAppResult

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws._

@Singleton
class HouseholdController @Inject()(
                                     implicit ws: WSClient,
                                     config: Configuration,
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  var household : Future[List[Household]] = Household.initTestData(20, config)

  /**
    * Reads all currently saved donations and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction.async { implicit request =>
    implicit val ec = ExecutionContext.global
    this.household.map(household => WebAppResult.Ok(Json.toJson(household)).toResult(request))
  }

  /**
    * Saves a given donation on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(parse.json).async { implicit request => {
    implicit val ec = ExecutionContext.global
    Future.successful(request.body.validate[Household].fold(
      errors => WebAppResult.BadRequest(errors).toResult(request),
      household => {
        this.household = this.household.map(_ :+ household)
        WebAppResult.Ok(Json.toJson(List(household))).toResult(request)
      }
    ))
  }}

  def update = silhouette.SecuredAction(parse.json).async { implicit request =>
    implicit val ec = ExecutionContext.global
    request.body.validate[Household].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      household => {
        this.household = this.household.map(_.map(entry => entry.id match {
          case household.id => household
          case _ => entry
        }))
        this.household.map(_ =>
          WebAppResult.Ok(Json.toJson(List(household))).toResult(request)
        )
      }
    )
  }
}