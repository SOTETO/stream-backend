package daos

import java.util.UUID

import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient
import utils.{Ascending, Descending, SortDir}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait UserDAO {
  def sortByCrew(userIds: List[UUID], dir: SortDir = Ascending) : Future[List[UUID]]
  def updateDatasource(userId : UUID): Future[Boolean]
}

class DropsUserDAO @Inject()(implicit ws: WSClient, config: Configuration) extends UserDAO {
  implicit val ec = ExecutionContext.global

  val path : String = config.get[String]("drops.rest.base") + config.get[String]("drops.rest.user.path")
  val client_id = config.get[String]("drops.client_id")
  val client_secret = config.get[String]("drops.client_secret")

  var source : DropsDatasource = DropsDatasource(Nil)

  case class UserCrewRequest(userIds: List[UUID], dir: SortDir = Ascending) {
//    println(this.toString)

    override def toString: String = Json.obj(
      "query" -> this.toQuery,
      "values" -> this.toValues,
      "sort" -> this.toSort
    ).toString()

    def toQuery : String = userIds.indices.map("user.publicId." + _ + ".=").mkString("_OR_")
    def toValues : JsValue = Json.obj("user" -> Json.obj(
      "publicId" -> userIds.zipWithIndex.map(_.swap).foldLeft[JsObject](Json.obj())((json, i_id) =>
        json ++ Json.obj(i_id._1.toString -> Json.toJson(i_id._2))
      )
    ))
    def toSort : JsValue =
      Json.obj("attributes" -> Json.toJson(List("SupporterCrew_name")), "dir" -> dir.name)
  }

  object UserCrewRequest {
    implicit val crewRequestWrites: Writes[UserCrewRequest] = (
      (JsPath \ "query").write[String] and
        (JsPath \ "values").write[JsValue] and
        (JsPath \ "sort").write[JsValue]
    )((request: UserCrewRequest) => (request.toQuery, request.toValues, request.toSort))
  }

  case class UserResponse(id: UUID)
  object UserResponse {
    implicit val userResponseFormat = Json.format[UserResponse]
  }

  case class DropsDatasource(userIds: List[UUID]) {
    private val sortedByClassAscending : Future[List[UUID]] = init(Ascending)
    private val sortedByClassDescending : Future[List[UUID]] = init(Descending)

    private def init(dir: SortDir): Future[List[UUID]] = ws.url(path)
      .addQueryStringParameters("client_id" -> client_id, "client_secret" -> client_secret)
      .post(Json.toJson(UserCrewRequest(userIds, dir)))
      .map(res => res.json.validate[List[UserResponse]].map(_.map(_.id)).fold(
        invalid => {
          println(invalid)
          Nil
        }, // Todo: something meaningful!
        valid => valid
      ))

    def ~ (userIds: List[UUID]): Boolean = this.userIds == userIds

    def get(dir: SortDir) : Future[List[UUID]] = dir match {
      case Ascending => this.sortedByClassAscending
      case Descending => this.sortedByClassDescending
    }

    def :+ (id: UUID) : DropsDatasource = DropsDatasource(this.userIds :+ id)
    def +: (id: UUID) : DropsDatasource = DropsDatasource(this.userIds.+:(id))
  }

  override def sortByCrew(userIds: List[UUID], dir: SortDir = Ascending): Future[List[UUID]] = {
    if (!(this.source ~ userIds)) {
      this.source = DropsDatasource(userIds)
    }
    this.source.get(dir)
  }

  override def updateDatasource(userId: UUID): Future[Boolean] = {
    val originalSize = this.source.get(Ascending).map(_.size)
    this.source = this.source :+ userId
    this.source.get(Ascending).map(_.size).flatMap(newSize => originalSize.map(newSize > _))
  }
}