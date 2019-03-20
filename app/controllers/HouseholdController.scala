package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import org.vivaconagua.play2OauthClient.silhouette.{CookieEnv, UserService}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.frontend.Household
import play.api.libs.json.Json
import responses.WebAppResult

import scala.concurrent.Future

@Singleton
class HouseholdController @Inject()(
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  var household : List[Household] = Nil

  /**
    * Reads all currently saved donations and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction.async { implicit request =>
    Future.successful(WebAppResult.Ok(Json.toJson(this.household)).toResult(request))
  }

  /**
    * Saves a given donation on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(parse.json).async { implicit request => {
    Future.successful(request.body.validate[Household].fold(
      errors => WebAppResult.BadRequest(errors).toResult(request),
      household => {
        this.household = this.household :+ household
        WebAppResult.Ok(Json.toJson(List(household))).toResult(request)
      }
    ))
  }}

  def update = silhouette.SecuredAction(parse.json).async { implicit request => {
    Future.successful(request.body.validate[Household].fold(
      errors => WebAppResult.BadRequest(errors).toResult(request),
      household => {
        this.household = this.household.map(entry => entry.id match {
          case household.id => household
          case _ => entry
        })
        WebAppResult.Ok(Json.toJson(List(household))).toResult(request)
      }
    ))
  }}
}