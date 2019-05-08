package models.frontend

import java.util.UUID

import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import models.TestData
import play.api.{ConfigLoader, Configuration}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.ws.WSClient
import utils.{Filterable, FilterableField}

case class HouseholdAmount(amount: Double, currency: String) {
  def isDefined : Boolean = this.amount > 0
}

case class Reason(what: Option[String], wherefor: Option[String]) {
  def isDefined : Boolean = this.what.isDefined && this.what.get != "" && this.wherefor.isDefined && this.wherefor != ""
}

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
                           ) {
  def isComplete : Boolean =
    iban.isDefined && iban != "" && bic.isDefined && bic != "" && amount.isDefined && reason.isDefined

  def isRequest : Boolean = this.request

  def volunteerManager(user: UUID) : HouseholdVersion = this.copy(volunteerManager = Some(user))
  def employee(user: UUID) : HouseholdVersion = this.copy(employee = Some(user))
  def editor(user: UUID) : HouseholdVersion = this.copy(editor = Some(user))
  def author(user: UUID): HouseholdVersion = this.copy(author = Some(user))
}

/**
  * DEPRECATED!
  * @author Johann Sell
  * @param name
  * @param tokens
  */
case class PetriNetPlace(name: String, tokens: Int) {
  def >= (o: scala.Any): Boolean = o match {
    case other: PetriNetPlace => this.name == other.name && this.tokens >= other.tokens
    case _ => false
  }
}

case class Household(
                    id: UUID,
                    state: PetriNetHouseholdState, //List[PetriNetPlace],
                    versions: List[HouseholdVersion]
                    ) {
  def addVersion(version: HouseholdVersion) : Household =
    Household(this.id, this.state, this.versions :+ version)

  def setAuthor(author: UUID): Household =
    this.copy(versions = versions.reverse.tail.reverse :+ versions.last.author(author))


  /**
    * Update this household and save the ID of the updating user.
    *
    * @author Johann Sell
    * @param user
    * @return
    */
  def update(user: UUID) : Household =
    this.copy(versions = versions.reverse.tail.reverse :+ versions.last.editor(user))

  /**
    * Add a new version considering the changing user of the state update.
    *
    * @author Johann Sell
    * @param state
    * @param user
    * @param role
    * @return
    */
  def setNewState(state: PetriNetHouseholdState, user: UUID, role: String) : Household = role match {
    case "volunteerManager" => this.copy(
      state = state, versions = versions :+ versions.last.volunteerManager(user)
    )
    case "employee" => this.copy(
      state = state, versions = versions :+ versions.last.employee(user)
    )
    case "editor" => this.copy(
      state = state, versions = versions :+ versions.last.editor(user)
    )
  }

  /**
    * Update the last version considering the changing user od the state update.
    *
    * @author Johann Sell
    * @param state
    * @param user
    * @param role
    * @return
    */
  def updateStateByEditor(state: PetriNetHouseholdState, user: UUID, role: String) : Household = role match {
    case "volunteerManager" => this.copy(
      state = state, versions = versions.reverse.tail.reverse :+ versions.last.volunteerManager(user)
    ) // versions.reverse.tail.reverse returns a copy of the list of versions without the last one
    case "employee" => this.copy(
      state = state, versions = versions.reverse.tail.reverse :+ versions.last.employee(user)
    )
    case "editor" => this.copy(
      state = state, versions = versions.reverse.tail.reverse :+ versions.last.editor(user)
    )
  }
}

object HouseholdAmount extends TestData[HouseholdAmount] {
  implicit val householdAmountFormat = Json.format[HouseholdAmount]

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

object Reason extends TestData[Reason] {
  implicit val reasonFormat = Json.format[Reason]

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

object HouseholdVersion extends TestData[HouseholdVersion] {
  implicit val householdVersionFormat = Json.format[HouseholdVersion]

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

object PetriNetPlace extends TestData[List[PetriNetPlace]] {
  implicit val petriNetPlaceFormat = Json.format[PetriNetPlace]

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[List[PetriNetPlace]]] = {
    Future.successful((0 to count).map(_ => List(PetriNetPlace("ProcessState.AppliedFor", 1), PetriNetPlace("VolunteerManager.Idle", 1))).toList)
  }
}

object Household extends TestData[Household] with Filterable {
//  implicit val householdFormat = Json.format[Household]

  implicit val householdReads : Reads[Household] = (
    (JsPath \ "id").read[UUID] and
      (JsPath \ "state").read[Seq[PlaceMessage]] and
      (JsPath \ "actions").read[Seq[ActionMessage]] and
      (JsPath \ "versions").read[Seq[HouseholdVersion]]
  )((uuid, placeMessages, actionMessages, versions) =>
    Household(uuid, PetriNetHouseholdState(placeMessages.toSet), versions.toList)
  )

  implicit val householdWrites : Writes[Household] = (
    (JsPath \ "id").write[UUID] and
      (JsPath \ "state").write[Seq[PlaceMessage]] and
      (JsPath \ "actions").write[Seq[ActionMessage]] and
      (JsPath \ "versions").write[Seq[HouseholdVersion]]
  )((household: Household) =>
    (household.id, household.state.toMessages, household.state.allAllowed.toSeq, household.versions.toSeq)
  )

  implicit val ec = ExecutionContext.global

  override def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[Household]] = {
    val r = scala.util.Random
    (0 to count).foldLeft[Future[List[Household]]](Future.successful(Nil))((testData, i) =>
      HouseholdVersion.initTestData(r.nextInt(3), config).flatMap(versions =>
        testData.map(_ :+ Household(UUID.randomUUID(), PetriNetHouseholdState(versions.lastOption), versions))
      )
    )
  }

  /**
    * Implements the list of {{{FilterableField}}} for {{{Household}}}.
    */
  override val filterable: List[FilterableField] = List(
    FilterableField("household.what"), FilterableField("household.wherefor"), FilterableField("household.crew"),
    FilterableField("household.amount"), FilterableField("household.created"), FilterableField("household.updated")
  )
}