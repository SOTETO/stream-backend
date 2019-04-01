package models.frontend

import java.util.UUID

import com.typesafe.config.Config
import controllers.restQuery.values.{FilterField, Filterable, RESTFilter}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import models.TestData
import play.api.{ConfigLoader, Configuration}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.ws.WSClient

case class HouseholdAmount(amount: Double, currency: String)

case class Reason(what: Option[String], wherefor: Option[String])

case class HouseholdVersion(
                             iban: Option[String],
                             bic: Option[String],
                             created: Long,
                             updated: Long,
                             author: Option[UUID],
                             editor: Option[UUID],
                             amount: HouseholdAmount,
                             reason: Reason,
                             request: Boolean,
                             volunteerManager: Option[UUID],
                             employee: Option[UUID]
                           )

case class PetriNetPlace(name: String, tokens: Int)

case class Household(
                    id: UUID,
                    state: List[PetriNetPlace],
                    versions: List[HouseholdVersion]
                    ) {
  def addVersion(version: HouseholdVersion) : Household =
    Household(this.id, this.state, this.versions :+ version)
}

object HouseholdAmount extends Filterable with TestData[HouseholdAmount] {
  implicit val householdAmountFormat = Json.format[HouseholdAmount]

  override val filterFields: List[FilterField] = List(
    FilterField("household", "amount"), FilterField("household", "currency")
  )

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[HouseholdAmount]] = {
    val r = scala.util.Random
    val currencies = config.get[Seq[String]]("testData.currency")

    Future.successful((0 to count).foldLeft[List[HouseholdAmount]](Nil)((testData, _) => {
      val lowerBound = 5
      val upperBound = 200
      val euro = lowerBound + r.nextInt((upperBound - lowerBound) + 1)
      val cent = BigDecimal(r.nextDouble()).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      testData ++ List(HouseholdAmount(euro + cent, currencies(r.nextInt(currencies.size))))
    }))
  }
}

object Reason extends Filterable with TestData[Reason] {
  implicit val reasonFormat = Json.format[Reason]

  override val filterFields: List[FilterField] = List(
    FilterField("household", "what"), FilterField("household", "wherefor")
  )

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[Reason]] = {
    val r = scala.util.Random
    val what = config.get[Seq[String]]("testData.household.reason.what")
    val wherefor = config.get[Seq[String]]("testData.household.reason.wherefor")
    Future.successful((0 to count).foldLeft[List[Reason]](Nil)((reasons, i) => reasons ++ List(
      Reason(
        what = r.nextBoolean() || (i > 0 && count % i == 0) match {
          case true => Some(what(r.nextInt(what.size)))
          case _ => None
        },
        wherefor = r.nextBoolean() || (i > 0 && count % i == 0) match {
          case true => Some(wherefor(r.nextInt(wherefor.size)))
          case _ => None
        }
      )
    )))
  }
}

object HouseholdVersion extends Filterable with TestData[HouseholdVersion] {
  implicit val householdVersionFormat = Json.format[HouseholdVersion]

  override val filterFields: List[FilterField] = List(
    FilterField("household", "iban"), FilterField("household", "bic"), FilterField("household", "created"),
    FilterField("household", "updated"), FilterField("household", "author"), FilterField("household", "editor"),
    FilterField("household", "request"), FilterField("household", "volunteerManager"), FilterField("household", "employee")
  )

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[HouseholdVersion]] = {
    val r = scala.util.Random
    val iban = config.get[Seq[String]]("testData.household.iban")
    val bic = config.get[Seq[String]]("testData.household.bic")
    val created = System.currentTimeMillis()

    implicit val ec = ExecutionContext.global

    val users = ws.url("http://localhost/drops/rest/user")
      .addHttpHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
      .addQueryStringParameters("client_id" -> "stream", "client_secret" -> "stream")
      .withRequestTimeout(10000.millis)
      .post(Json.obj("limit" -> 20))
      .map {
        response => (response.json \\ "id").map(_.as[UUID])
      }

    users.flatMap(ul =>
      HouseholdAmount.initTestData(1, config).flatMap(amount =>
        Reason.initTestData(1, config).map(reason =>
          (0 to count).foldLeft[List[HouseholdVersion]](Nil)((versions, i) => versions ++ List(
            HouseholdVersion(
              iban = r.nextBoolean() match {
                case true => Some(iban(r.nextInt(iban.size)))
                case _ => None
              },
              bic = r.nextBoolean() match {
                case true => Some(bic(r.nextInt(bic.size)))
                case _ => None
              },
              created = created,
              updated = i == 0 match {
                case true => created
                case _ => System.currentTimeMillis()
              },
              amount = amount.head,
              reason = reason.head,
              request = r.nextBoolean(),
              author = i == 0 match {
                case true => Some(ul(r.nextInt(ul.size)))
                case _ => None
              },
              editor = i == 0 match {
                case true => None
                case _ => Some(ul(r.nextInt(ul.size)))
              },
              volunteerManager = None,
              employee = None
            )
          ))
        )
      )
    )
  }
}

object PetriNetPlace extends Filterable with TestData[List[PetriNetPlace]] {
  implicit val petriNetPlaceFormat = Json.format[PetriNetPlace]

  override val filterFields: List[FilterField] = List(
    FilterField("state", "place"), FilterField("state", "tokens")
  )

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[List[PetriNetPlace]]] = {
    Future.successful((0 to count).map(_ => List(PetriNetPlace("ProcessState.AppliedFor", 1), PetriNetPlace("VolunteerManager.Idle", 1))).toList)
  }
}

object Household extends Filterable with TestData[Household] {
  implicit val householdFormat = Json.format[Household]

  override val filterFields: List[FilterField] = List(
    FilterField("household", "id")
  )

  override val dependentFilter: List[Reads[RESTFilter]] = List(
    PetriNetPlace.filterJson, HouseholdVersion.filterJson, Reason.filterJson, HouseholdAmount.filterJson
  )

  implicit val ec = ExecutionContext.global

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[Household]] = {
    val r = scala.util.Random
    (0 to count).foldLeft[Future[List[Household]]](Future.successful(Nil))((testData, i) =>
      PetriNetPlace.initTestData(1, config).flatMap(petriNet =>
        HouseholdVersion.initTestData(r.nextInt(3), config).flatMap(versions =>
          testData.map(_ :+ Household(UUID.randomUUID(), petriNet.head, versions))
        )
      )
    )
  }
}