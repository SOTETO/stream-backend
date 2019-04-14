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

/**
  * Interface for a UserDAO
  *
  * @author Johann Sell
  */
trait UserDAO {
  /**
    * Sorts the given UUIDs of users by their crews name.
    *
    * @author Johann Sell
    * @param userIds list of users UUIDs
    * @param dir indicates if ascending or descending sortation
    * @return
    */
  def sortByCrew(userIds: List[UUID], dir: SortDir = Ascending) : Future[List[UUID]]

  /**
    * Updates the set of considered users UUIDs. Since the users are obtained from Drops, this function is used to update
    * the temporary memory.
    *
    * @author Johann Sell
    * @param userId the newly added authors UUID
    * @return
    */
  def updateDatasource(userId : UUID): Future[Boolean]
}

/**
  * Implements the connection to Drops.
  *
  * @author Johann Sell
  * @param ws Webservice client
  * @param config Configuration of Stream
  */
class DropsUserDAO @Inject()(implicit ws: WSClient, config: Configuration) extends UserDAO {
  implicit val ec = ExecutionContext.global

  val path : String = config.get[String]("drops.rest.base") + config.get[String]("drops.rest.user.path")
  val client_id = config.get[String]("drops.client_id")
  val client_secret = config.get[String]("drops.client_secret")

  var source : DropsDatasource = DropsDatasource(Nil)

  /**
    * Represents a request to Drops.
    *
    * @author Johann Sell
    * @param userIds list of currently considered users (authors of household entries)
    * @param dir indicates if ascending or descending sortation
    */
  case class UserCrewRequest(userIds: List[UUID], dir: SortDir = Ascending) {
//    println(this.toString)

    /**
      * Generates a JSON string equivalent to the request body.
      *
      * @author Johann Sell
      * @return
      */
    override def toString: String = Json.obj(
      "query" -> this.toQuery,
      "values" -> this.toValues,
      "sort" -> this.toSort
    ).toString()

    /**
      * Generates the query string.
      *
      * @author Johann Sell
      * @return
      */
    def toQuery : String = userIds.indices.map("user.publicId." + _ + ".=").mkString("_OR_")

    /**
      * Generates the JSON that can be used as values attribute of a drops REST query.
      *
      * @author Johann Sell
      * @return
      */
    def toValues : JsValue = Json.obj("user" -> Json.obj(
      "publicId" -> userIds.zipWithIndex.map(_.swap).foldLeft[JsObject](Json.obj())((json, i_id) =>
        json ++ Json.obj(i_id._1.toString -> Json.toJson(i_id._2))
      )
    ))

    /**
      * Generates the JSON that can be used as sort attribute of a drops REST query.
      * @return
      */
    def toSort : JsValue =
      Json.obj("attributes" -> Json.toJson(List("SupporterCrew_name")), "dir" -> dir.name)
  }

  /**
    * Companion object for request class.
    *
    * @author Johann Sell
    */
  object UserCrewRequest {
    /**
      * Generates JSON (Writes)
      *
      * @author Johann Sell
      */
    implicit val crewRequestWrites: Writes[UserCrewRequest] = (
      (JsPath \ "query").write[String] and
        (JsPath \ "values").write[JsValue] and
        (JsPath \ "sort").write[JsValue]
    )((request: UserCrewRequest) => (request.toQuery, request.toValues, request.toSort))
  }

  /**
    * Represents a Drops REST response.
    *
    * @author Johann Sell
    * @param id
    */
  case class UserResponse(id: UUID)

  /**
    * Companion object for a Drops REST repsonse.
    *
    * @author Johann Sell
    */
  object UserResponse {
    implicit val userResponseFormat = Json.format[UserResponse]
  }

  /**
    * Handles the Drops REST requests and saves the result temporally.
    *
    * @author Johann Sell
    * @param userIds list of users UUIDs that have to be sorted
    */
  case class DropsDatasource(userIds: List[UUID]) {
    private val sortedByClassAscending : Future[List[UUID]] = init(Ascending)
    private val sortedByClassDescending : Future[List[UUID]] = init(Descending)

    /**
      * Calls Drops RESTful webservice.
      *
      * @author Johann Sell
      * @param dir indicates if ascending or descending sortation
      * @return
      */
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

    /**
      * Indicates if a set of users UUIDs is represented by this Datasource instance.
      *
      * @author Johann Sell
      * @param userIds list of users UUIDs
      * @return
      */
    def ~ (userIds: List[UUID]): Boolean = this.userIds == userIds

    /**
      * Returns the represented users UUIDs sorted by the given direction.
      *
      * @author Johann Sell
      * @param dir indicates if ascending or descending sortation
      * @return
      */
    def get(dir: SortDir) : Future[List[UUID]] = dir match {
      case Ascending => this.sortedByClassAscending
      case Descending => this.sortedByClassDescending
    }

    /**
      * Appends an additional users UUID to the set. The function returns a new Datasource instance.
      *
      * @author Johannn Sell
      * @param id a users UUID
      * @return
      */
    def :+ (id: UUID) : DropsDatasource = DropsDatasource(this.userIds :+ id)


    /**
      * Prepends an additional users UUID to the set. The function returns a new Datasource instance.
      *
      * @author Johannn Sell
      * @param id a users UUID
      * @return
      */
    def +: (id: UUID) : DropsDatasource = DropsDatasource(this.userIds.+:(id))
  }

  /**
    * Sorts a given list of UUIDs by their users crew names.
    *
    * @author Johann Sell
    * @param userIds list of users UUIDs
    * @param dir indicates if ascending or descending sortation
    * @return
    */
  override def sortByCrew(userIds: List[UUID], dir: SortDir = Ascending): Future[List[UUID]] = {
    if (!(this.source ~ userIds)) {
      this.source = DropsDatasource(userIds)
    }
    this.source.get(dir)
  }

  /**
    * Generates a new Datasource instance by adding the newly given UUID.
    *
    * @author Johann Sell
    * @param userId the newly added authors UUID
    * @return
    */
  override def updateDatasource(userId: UUID): Future[Boolean] = {
    val originalSize = this.source.get(Ascending).map(_.size)
    this.source = this.source :+ userId
    this.source.get(Ascending).map(_.size).flatMap(newSize => originalSize.map(newSize > _))
  }
}