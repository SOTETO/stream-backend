package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import com.mohiva.play.silhouette.api.Silhouette
import org.vivaconagua.play2OauthClient.silhouette.CookieEnv
import org.vivaconagua.play2OauthClient.silhouette.UserService
import play.api.libs.json.{JsError, Json, Reads}
import models.frontend.Donation
import responses.WebAppResult

import scala.concurrent.Future

@Singleton
class DonationsController @Inject()(
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  var donations : List[Donation] = Nil

  /**
    * Reads all currently saved donations and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction.async { implicit request =>
    Future.successful(WebAppResult.Ok(Json.toJson(this.donations)).toResult(request))
  }

  /**
    * Saves a given donation on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(parse.json).async { implicit request => {
    Future.successful(request.body.validate[Donation].fold(
      errors => WebAppResult.BadRequest(errors).toResult(request),
      donation => {
        this.donations = this.donations :+ donation
        WebAppResult.Ok(Json.toJson(List(donation))).toResult(request)
      }
    ))
  }}
}
