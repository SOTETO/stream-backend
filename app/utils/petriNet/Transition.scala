package utils.petriNet

/**
  * Represents a transition of a Petri Net. It consists of a name, a map of places and token describing the preconditions
  * of the transition that have to be fulfilled to fire, and a map of places and token that describe the producing activity
  * of the transition on fire.
  *
  * The consumers maps from a place to a token and means that the transition can fire if and only if, the place holds the
  * same or a bigger amount of tokens as described by the token it is pointing through. The producers maps from a place
  * to a token and means that the place adds the amount of tokens it is pointing through to the existing amount of the
  * place.
  *
  * @author Johann Sell
  * @param name
  * @param consumes
  * @param produces
  */
case class Transition(name: String, consumes: Map[Place, Token], produces: Map[Place, Token]) {

  /**
    * Checks if the given [[Transition]] or [[java.lang.String]] represents the same transition as this.
    *
    * @author Johann Sell
    * @param other
    * @return
    */
  def ~ (other: Any) : Boolean = other match {
    case o: Transition => this.name == o.name
    case o: String => this.name == o
    case _ => false
  }

  /**
    * All preconditions fulfilled?
    *
    * @author Johann Sell
    * @return
    */
  def isAllowed : Boolean = consumes.forall(pair => pair._1 >= pair._2)

  /**
    * Fires the transition and returns a new instance with updated places.
    *
    * @author Johann Sell
    * @return
    */
  def fire : Transition = isAllowed match {
    case true => Transition(
      this.name,
      this.consumes.map(pair => (pair._1 - pair._2, pair._2)),
      this.produces.map(pair => (pair._1 + pair._2, pair._2))
    )
    case false => this
  }

  /**
    * Replaces all references to a place by the references to another instance representing the same place that is part
    * of a given transition.
    *
    * This is required since the `fire` methods returns a new instance of `this` with also new instances of the
    * consumer and producer places.
    *
    * @author Johann Sell
    * @param other
    * @return
    */
  def <-- (other: Transition) : Transition = Transition(
    this.name,
    // replace all references to places in this by the potentially updated copies of the same place
    this.consumes.map(pair => other.consumes.keySet.find(_ ~ pair._1) match {
      case Some(place) => (place, pair._2)
      case None => other.produces.keySet.find(_ ~ pair._1) match {
        case Some(place) => (place, pair._2)
        case None => pair
      }
    }),
    // replace all references to places in this by the potentially updated copies of the same place
    this.produces.map(pair => other.produces.keySet.find(_ ~ pair._1) match {
      case Some(place) => (place, pair._2)
      case None => other.consumes.keySet.find(_ ~ pair._1) match {
        case Some(place) => (place, pair._2)
        case None => pair
      }
    })
  )

  override def toString: String =
    this.consumes.zip(this.produces).map(inOut =>
      inOut._1._1 + " ---> " + inOut._1._2.amount + " ---> " + this.name + " ---> " + inOut._2._2 + " ---> " + inOut._2._1
    ).mkString("\n")
}
