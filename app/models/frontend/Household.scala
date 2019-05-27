package models.frontend

import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._
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

case class Household(
                    id: UUID,
                    state: PetriNetHouseholdState,
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

object HouseholdAmount {
  implicit val householdAmountFormat = Json.format[HouseholdAmount]
}

object Reason {
  implicit val reasonFormat = Json.format[Reason]
}

object HouseholdVersion {
  implicit val householdVersionFormat = Json.format[HouseholdVersion]
}

object Household extends Filterable {

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

  /**
    * Implements the list of {{{FilterableField}}} for {{{Household}}}.
    */
  override val filterable: List[FilterableField] = List(
    FilterableField("household.what"), FilterableField("household.wherefor"), FilterableField("household.crew"),
    FilterableField("household.amount"), FilterableField("household.created"), FilterableField("household.updated")
  )
}