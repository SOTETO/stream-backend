package models.frontend

import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.JsValueWrapper

case class HouseholdAmount(amount: Double, formatted: String)

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
                    )

object HouseholdAmount {
  implicit val householdAmountFormat = Json.format[HouseholdAmount]
}

object Reason {
  implicit val reasonFormat = Json.format[Reason]
}

object HouseholdVersion {
  implicit val householdVersionFormat = Json.format[HouseholdVersion]
}

object PetriNetPlace {
  implicit val petriNetPlaceFormat = Json.format[PetriNetPlace]
}

object Household {
  implicit val householdFormat = Json.format[Household]
}