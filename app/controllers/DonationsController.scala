package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import com.mohiva.play.silhouette.api.Silhouette
import org.vivaconagua.play2OauthClient.silhouette.CookieEnv
import org.vivaconagua.play2OauthClient.silhouette.UserService
import play.api.libs.json.{JsError, Json, Reads}
import play.api.Configuration
import models.frontend.Donation
import responses.WebAppResult
import service.DonationsService
import utils.{Ascending, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DonationsController @Inject()(
                                     cc: ControllerComponents,
                                     silhouette: Silhouette[CookieEnv],
                                     userService: UserService,
                                     service: DonationsService
                                   ) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  implicit val ec = ExecutionContext.global

  /**
    * Reads all currently saved donations and returns them.
    *
    * @author Johann Sell
    * @return
    */
  def get = silhouette.SecuredAction(parse.json).async { implicit request =>
    request.body.validate[QueryBody].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      query => service.all(query.page, query.sort).map(donations => WebAppResult.Ok(Json.toJson(donations)).toResult(request))
    )
  }

  /**
    * Saves a given donation on the server and returns it after successful saving.
    *
    * @author Johann Sell
    * @return
    */
  def create = silhouette.SecuredAction(parse.json).async { implicit request => {
    request.body.validate[Donation].fold(
      errors => Future.successful(WebAppResult.BadRequest(errors).toResult(request)),
      donation => {
        service.save(donation).map(_ match {
          case Right(databaseDonation) => WebAppResult.Ok(Json.toJson(List(databaseDonation))).toResult(request)
          case Left(exception) => WebAppResult.InternalServerError(exception).toResult(request)
        })
      }
    )
  }}
}
