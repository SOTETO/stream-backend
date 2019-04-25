package utils

import models.frontend.{Household, PetriNetPlace}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CompleteFilter(complete: Boolean, incomplete: Boolean) {
  def ? (household: Household) : Boolean = {
    (complete && household.state.foldLeft[Boolean](false)((found, place) => found || place >= PetriNetPlace("ProcessState.HouseholdComplete", 1))) ||
      (incomplete && !household.state.foldLeft[Boolean](false)((found, place) => found || place >= PetriNetPlace("ProcessState.HouseholdComplete", 1))) ||
      (!complete && !incomplete)
  }
}

object CompleteFilter {
  implicit val completeFilterFormat = Json.format[CompleteFilter]
}

case class HouseholdFilter(
                          what: Option[String],
                          wherefor: Option[String],
                          amount: Option[Double],
                          complete: Option[CompleteFilter],
                          repayment: List[PetriNetPlace],
                          volunteerManager: List[PetriNetPlace],
                          employee: List[PetriNetPlace]
                          ) {
  def ? (household: Household) : Boolean = {
    def compareAmount(amount: Double, query: Double): Boolean = {
      val length = query.floor < query match {
        case true => query.toString.length - 1
        case false => query.toString.indexOf(".") - 1
      }
      (0 to length).foldLeft[Boolean](true)((currently, position) => {
        amount.toString.length > position && currently && query.toString.charAt(position) == amount.toString.charAt(position)
      })
    }


    what.map(query => household.versions.lastOption.map(_.reason.what.map(_.contains(query)).getOrElse(false)).getOrElse(false)).getOrElse(true) &&
    wherefor.map(query => household.versions.lastOption.map(_.reason.wherefor.map(_.contains(query)).getOrElse(false)).getOrElse(false)).getOrElse(true) &&
    amount.map(query => household.versions.lastOption.map(version => compareAmount(version.amount.amount, query)).getOrElse(false)).getOrElse(true) &&
    complete.map(query => query ? household).getOrElse(true) &&
    repayment.map(query => household.state.contains((place: PetriNetPlace) => place >= query)).filter(!_).size == 0 &&
    volunteerManager.map(query => household.state.contains((place: PetriNetPlace) => place >= query)).filter(!_).size == 0 &&
    employee.map(query => household.state.contains((place: PetriNetPlace) => place >= query)).filter(!_).size == 0
  }
}
object HouseholdFilter {

  implicit val sortReads: Reads[HouseholdFilter] = (
    (JsPath \ "what").readNullable[String] and
      (JsPath \ "wherefor").readNullable[String] and
      (JsPath \ "amount").readNullable[Double] and
      (JsPath \ "complete").readNullable[CompleteFilter] and
      (JsPath \ "repayment").readNullable[List[PetriNetPlace]] and
      (JsPath \ "volunteerManager").readNullable[List[PetriNetPlace]] and
      (JsPath \ "employee").readNullable[List[PetriNetPlace]]
    ).tupled.map(t => HouseholdFilter(t._1, t._2, t._3, t._4, t._5.getOrElse(Nil), t._6.getOrElse(Nil), t._7.getOrElse(Nil)))

  implicit val sortWrites: Writes[HouseholdFilter] = (
    (JsPath \ "what").writeNullable[String] and
      (JsPath \ "wherefor").writeNullable[String] and
      (JsPath \ "amount").writeNullable[Double] and
      (JsPath \ "complete").writeNullable[CompleteFilter] and
      (JsPath \ "repayment").writeNullable[List[PetriNetPlace]] and
      (JsPath \ "volunteerManager").writeNullable[List[PetriNetPlace]] and
      (JsPath \ "employee").writeNullable[List[PetriNetPlace]]
    )(filter => (filter.what, filter.wherefor, filter.amount, filter.complete,
        filter.repayment.size match {
          case 0 => None
          case _ => Some(filter.repayment)
        },
        filter.volunteerManager.size match {
          case 0 => None
          case _ => Some(filter.volunteerManager)
        },
        filter.employee.size match {
          case 0 => None
          case _ => Some(filter.employee)
        }
    ))
}
