package models.frontend

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.petriNet.{PetriNet, Transition}

case class ActionMessageNotExists(msg: String) extends Exception(msg)

case class ActionMessage(name: String) {

  override def equals(o: scala.Any): Boolean = o match {
    case other: ActionMessage => this.name == other.name
    case _ => false
  }

  def ~ (n: String) : Boolean = name == n

  def toExecuter(state: PetriNetHouseholdState): Either[ActionMessageNotExists,ActionMessageExecuter] =
    ActionMessageExecuter.from(this.name, state)
}

object ActionMessage {
  implicit val actionMessageFormat = Json.format[ActionMessage]
}

case class ActionMessageExecuter(msg: ActionMessage, transitions: Set[Transition]) {

  override def equals(o: scala.Any): Boolean = o match {
    case other: ActionMessageExecuter => this.msg == other.msg
    case other: ActionMessage => this.msg == other
    case _ => false
  }

  def ~ (o: scala.Any) : Boolean = o match {
    case other: ActionMessage => this.msg == other
    case name : String => msg ~ name
  }

  def isAllowed: Boolean = transitions.exists(_.isAllowed)

  def transform(petriNet: PetriNet) : PetriNet = transitions.find(_.isAllowed) match {
    case Some(transition) => petriNet.fire(transition.name)
    case None => petriNet
  }

  def update(petriNet : PetriNet) : ActionMessageExecuter = ActionMessageExecuter(
    this.msg,
    this.transitions.map(transition => petriNet.transitions.find(_ ~ transition).getOrElse(transition))
  )
}

object ActionMessageExecuter {
  def apply(name: String, transitions: Set[Transition]): ActionMessageExecuter =
    ActionMessageExecuter(ActionMessage(name), transitions)

  def from(name: String, state: PetriNetHouseholdState): Either[ActionMessageNotExists, ActionMessageExecuter] =
    state.actions.find(_ ~ name).map(Right( _ )).getOrElse(Left(ActionMessageNotExists("Action does not exists")))
}