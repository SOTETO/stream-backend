package utils.petriNet

case class PetriNet(transitions: Set[Transition]) {
  def isAllowed(name: String) : Boolean = {
    transitions.find(_ ~ name) match {
      case Some(t) => t.isAllowed
      case _ => false
    }
  }

  def fire(name: String) : PetriNet = this.isAllowed(name) match {
    case true => transitions.find(_ ~ name) match {
      case Some(transition) => this.replace(transition.fire)
      case None => this
    }
    case false => this
  }

  def ? (place: Place) : Boolean = transitions.foldLeft[Set[Place]](Set())((acc, t) =>
    acc ++ t.consumes.map(_._1) ++ t.produces.map(_._1)
  ).find(_ ~ place).map(_ >= place.token).getOrElse(false)

  def getPlaces : List[Place] = transitions.foldLeft[Set[Place]](Set())((acc, t) =>
    acc ++ t.consumes.map(_._1) ++ t.produces.map(_._1)
  ).toList

  override def toString: String = this.getPlaces.map(_.toString).mkString("\n")

  private def replace(transition: Transition) : PetriNet = PetriNet(transitions.map(t => t ~ transition match {
    case true => transition
    case false => t <-- transition
  }))
}