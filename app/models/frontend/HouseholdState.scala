package models.frontend

import java.util.function.ToDoubleBiFunction

import play.api.Configuration
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient
import utils.petriNet._

import scala.concurrent.Future

class TransformationNotAllowed(msg: String) extends Exception(msg)

/**
  * Represents a households state. It is implemented using the [[utils.petriNet.PetriNet]] implementation, but holds also a set of actions
  * (groups of transitions that are semantically equivalent).
  *
  * @author Johann Sell
  * @param petriNet
  * @param actions
  */
case class PetriNetHouseholdState(petriNet: PetriNet, actions: Set[ActionMessageExecuter]) {

  /**
    * Create a new state from a given transformation.
    *
    * @author Johann Sell
    * @param t
    * @return
    */
  def transform(t: ActionMessage) : Either[TransformationNotAllowed, PetriNetHouseholdState] =
    this.actions.find(action => action ~ t) match {
      case Some(action) => action.isAllowed match {
        case true => {
          val newPetriNet = action.transform(this.petriNet)
          Right(PetriNetHouseholdState(newPetriNet, this.actions.map(_.update(newPetriNet))))
        }
        case false => Left(new TransformationNotAllowed("Action not allowed."))
      }
      case None => Left(new TransformationNotAllowed("Action does not exists."))
    }

  /**
    * Checks if a given transformation is a allowed on this state.
    *
    * @author Johann Sell
    * @param t
    * @return
    */
  def isAllowed(t: ActionMessage): Boolean =
    this.actions.find(action => action ~ t) match {
      case Some(action) => action.isAllowed
      case None => false
    }

  /**
    * Returns a set of all currently allowed transformations.
    *
    * @author Johann Sell
    * @return
    */
  def allAllowed: Set[ActionMessage] = actions.filter(executer => isAllowed( executer.msg )).map(_.msg)

  /**
    * Checks if a household state fulfills the given place. Thus, it proofs if the corresponding place of the household
    * state holds equal or more tokens as required by the given place message.
    *
    * @author Johann Sell
    * @param place
    * @return
    */
  def ?(place: PlaceMessage): Boolean = petriNet ? place.toPlace

  /**
    * Transforms the state into a list of messages. Only relevant (tokens > 0) places will be returned.
    *
    * @author Johann Sell
    * @return
    */
  def toMessages : List[PlaceMessage] = petriNet.getPlaces.filter(_ > Token(0)).map(PlaceMessage( _ ))
}

object PetriNetHouseholdState {
  /**
    * Instanciates a [[PetriNetHouseholdState]] from a static description of a Petri Net. The Petri Nets state is
    * configurable by a given set of [[PlaceMessage]] instances.
    *
    * @author Johann Sell
    * @param placeMessages
    * @return
    */
  def apply(placeMessages : Set[PlaceMessage]) : PetriNetHouseholdState = {
    def getTokens(name: String) : Token = placeMessages.find(_ ~ name) match {
      case Some(msg) => Token(msg.tokens)
      case _ => Token(0)
    }
    def getPlace(name: String): Place = Place(name, getTokens(name))
    val (petriNet, actions) = {
      // Places
      val start = getPlace("Start")
      val appliedFor = getPlace("AppliedFor")
      val requested = getPlace("Requested")
      val approved = getPlace("Approved")
      val free = getPlace("Free")
      val complete = getPlace("HouseholdComplete")
      val repaid = getPlace("Repaid")
      val notEditable = getPlace("NotEditable")
      val refusedFromRequest = getPlace("RefusedFromRequest")
      val refusedFromApplication = getPlace("RefusedFromApplication")
      val vmIdle = getPlace("VM.Idle")
      val vmKnows = getPlace("VM.Knows")
      val vmKnowsNothing = getPlace("VM.KnowsNothing")

      // Transitions
      val apply = Transition("apply", Map(start -> Token(1)), Map(appliedFor -> Token(1)))
      val request = Transition("request", Map(start -> Token(1)), Map(requested -> Token(1)))
      val swap_to_request = Transition("swap_to_request", Map(appliedFor -> Token(1)), Map(requested -> Token(1)))
      val swap_to_appliedFor = Transition("swap_to_appliedFor", Map(requested -> Token(1)), Map(appliedFor -> Token(1)))

      val block_from_requested = Transition("block_from_requested", Map(requested -> Token(1)), Map(refusedFromRequest -> Token(1)))
      val block_from_appliedFor = Transition("block_from_appliedFor", Map(appliedFor -> Token(1)), Map(refusedFromApplication -> Token(1)))
      val block_from_approved = Transition("block_from_approved", Map(approved -> Token(1)), Map(refusedFromRequest -> Token(1)))
      val block_from_freed = Transition("block_from_freed", Map(free -> Token(1)), Map(refusedFromApplication -> Token(1)))

      val approve = Transition("approve", Map(requested -> Token(1)), Map(approved -> Token(1)))
      val freeing = Transition("free", Map(appliedFor -> Token(1)), Map(free -> Token(1)))
      val approve_from_blocked = Transition("approve_from_blocked", Map(refusedFromRequest -> Token(1)), Map(approved -> Token(1)))
      val free_from_blocked = Transition("free_from_blocked", Map(refusedFromApplication -> Token(1)), Map(free -> Token(1)))

      val request_payment = Transition("request_payment", Map(approved -> Token(1)), Map(free -> Token(1)))
      val repay = Transition("repay", Map(free -> Token(1), complete -> Token(1)), Map(repaid -> Token(1), notEditable -> Token(1), complete -> Token(1)))

      val knows = Transition("knows", Map(vmIdle -> Token(1)), Map(vmKnows -> Token(1)))
      val knows_nothing = Transition("knows_nothing", Map(vmIdle -> Token(1)), Map(vmKnowsNothing -> Token(1)))
      val from_knows_to_nothing = Transition("from_knows_to_nothing", Map(vmKnows -> Token(1)), Map(vmKnowsNothing -> Token(1)))
      val from_nothing_to_knows = Transition("from_nothing_to_knows", Map(vmKnowsNothing -> Token(1)), Map(vmKnows -> Token(1)))

      val completing = Transition("complete", Map(), Map(complete -> Token(1)))
      val incomplete = Transition("incomplete", Map(complete -> Token(1)), Map())

      (PetriNet(
        Set(
          apply, request, swap_to_request, swap_to_appliedFor,
          block_from_requested, block_from_appliedFor, block_from_approved, block_from_freed,
          approve, freeing, approve_from_blocked, free_from_blocked,
          request_payment, repay,
          knows, knows_nothing, from_knows_to_nothing, from_nothing_to_knows,
          completing, incomplete
        )
      ),
        Set(
          ActionMessageExecuter("apply", Set(apply, swap_to_appliedFor)),
          ActionMessageExecuter("request", Set(request, swap_to_request)),
          ActionMessageExecuter("free", Set(freeing, free_from_blocked)),
          ActionMessageExecuter("approve", Set(approve, approve_from_blocked)),
          ActionMessageExecuter("block", Set(block_from_requested, block_from_appliedFor, block_from_approved, block_from_freed)),
          ActionMessageExecuter("requestPayment", Set(request_payment)),
          ActionMessageExecuter("repay", Set(repay)),
          ActionMessageExecuter("isKnown", Set(knows, from_nothing_to_knows)),
          ActionMessageExecuter("isUnknown", Set(knows_nothing, from_knows_to_nothing)),
          ActionMessageExecuter("complete", Set(completing)),
          ActionMessageExecuter("incomplete", Set(incomplete))
        )
      )
    }
    PetriNetHouseholdState(petriNet, actions)
  }

  def apply(version: Option[HouseholdVersion]) : PetriNetHouseholdState = {
    val messages = Set(
      version.map(_.isRequest match {
        case true => PlaceMessage("Requested", 1)
        case false => PlaceMessage("AppliedFor", 1)
      }).getOrElse(PlaceMessage("Start", 1)),
      version.map(_.isComplete match {
        case true => PlaceMessage("HouseholdComplete", 1)
        case false => PlaceMessage("HouseholdComplete", 0)
      }).getOrElse(PlaceMessage("HouseholdComplete", 0)),
      PlaceMessage("VM.Idle", 1)
    )
    PetriNetHouseholdState(messages)
  }
}
