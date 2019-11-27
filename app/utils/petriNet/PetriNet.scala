package utils.petriNet

/**
  * Represents a Petri Net. It consists of a set of [[Transition]] since the [[Transition]] holds references to the [[Place]]
  * instances. This means, that no isolated places are allowed by this implementation!
  *
  * @author Johann Sell
  * @param transitions
  */
case class PetriNet(transitions: Set[Transition]) {
  def isAllowed(name: String) : Boolean = {
    transitions.find(_ ~ name) match {
      case Some(t) => t.isAllowed
      case _ => false
    }
  }

  /**
    * It fires a transition and updates all references by using `PetriNet.replace`.
    *
    * @author Johann Sell
    * @param name
    * @return
    */
  def fire(name: String) : PetriNet = this.isAllowed(name) match {
    case true => transitions.find(_ ~ name) match {
      case Some(transition) => this.replace(transition.fire)
      case None => this
    }
    case false => this
  }

  /**
    * Checks if a given [[Place]] is part of this Petri Net and if the place has the same or a bigger amount of tokens as
    * the given one.
    *
    * @author Johann Sell
    * @param place
    * @return
    */
  def ? (place: Place) : Boolean = transitions.foldLeft[Set[Place]](Set())((acc, t) =>
    acc ++ t.consumes.map(_._1) ++ t.produces.map(_._1)
  ).find(_ ~ place).map(_ >= place.token).getOrElse(false)

  /**
    * Returns a list of all places.
    *
    * @author Johann Sell
    * @return
    */
  def getPlaces : List[Place] = transitions.foldLeft[Set[Place]](Set())((acc, t) =>
    acc ++ t.consumes.map(_._1) ++ t.produces.map(_._1)
  ).toList

  override def toString: String = this.getPlaces.map(_.toString).mkString("\n")

  /**
    * Replace a subset of [[Place]] references of the Petri Net by the place references used by the given [[Transition]].
    *
    * @author Johann Sell
    * @param transition
    * @return
    */
  private def replace(transition: Transition) : PetriNet = PetriNet(transitions.map(t => t ~ transition match {
    case true => transition
    case false => t <-- transition
  }))
}
