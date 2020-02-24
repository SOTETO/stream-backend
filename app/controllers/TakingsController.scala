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
import models.frontend.{ Taking, TakingStub, Page, Sort, TakingQueryBody, TakingFilter, SortDir}
import responses.WebAppResult
import service.TakingsService
import utils.permissions.TakingPermission
import utils.Validate
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
  
  def validateUUID(uuid: String): Option[UUID] = {
    try{
      Some(UUID.fromString(uuid))
    }
    catch {
      case error: IllegalArgumentException => None
    }
  }

  def validateJson[A: Reads] = parser.json.validate(_.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e))))
  /**
    * Reads all currently saved takings and returns them.
    *
    * @author Johann Sell
    * @return
    */

  def getById(uuid: String) = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async { implicit request => {
    validateUUID(uuid) match {
      case Some(id) => { service.getById(UUID.fromString(uuid)).map(taking => taking match {
        case Some(t) => Ok(Json.toJson(t))
        case None => NotFound(Json.obj({ "ERROR" -> "Can't find taking with given id" }))
      })}
      case None => Future.successful(NotFound(Json.obj({ "ERROR" -> "Can't find taking with given id" })))
    }
  }}

  def get( 
    offset: Option[Int], 
    size: Option[Int],
    sortby: Option[String], 
    sortdir: Option[String],
    publicId: Option[String],
    name: Option[String],
    crew:Option[String],
    ato: Option[Double],
    afrom: Option[Double],
    exto: Option[Double],
    exfrom: Option[Double],
    cashto: Option[Double],
    cashfrom: Option[Double],
    confirmed: Option[Boolean],
    unconfirmed: Option[Boolean],
    open: Option[Boolean],
    payfrom: Option[Long],
    payto: Option[Long],
    crfrom: Option[Long],
    crto: Option[Long]
) = silhouette.SecuredAction(
    (IsVolunteerManager() && IsResponsibleFor("finance")) || IsEmployee || IsAdmin
  ).async { implicit request => {
    val sort:Sort = Sort(sortby.getOrElse(""), SortDir(sortdir.getOrElse("ASC")).get)
    val page: Page = Page(offset.getOrElse(0), size.getOrElse(20))
    val nameList: Option[List[String]] = name match {
      case Some(n) => Some(n.split(" ").toList)
      case _ => None 
    }
    val filter: TakingFilter = TakingFilter(
      Validate.isUUID(publicId), 
      nameList, 
      Validate.isUUID(crew), 
      ato, 
      afrom, 
      exto, 
      exfrom, 
      cashto, 
      cashfrom, 
      confirmed,
      unconfirmed,
      open,
      payfrom,
      payto,
      crfrom,
      crto
      )
    service.all(Some(page), Some(sort), permission.restrict(Some(filter), request.identity))
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
