package utils.petriNet

case class Transition(name: String, consumes: Map[Place, Token], produces: Map[Place, Token]) {
  def ~ (other: Any) : Boolean = other match {
    case o: Transition => this.name == o.name
    case o: String => this.name == o
    case _ => false
  }

  def isAllowed : Boolean = consumes.forall(pair => pair._1 >= pair._2)

  def fire : Transition = isAllowed match {
    case true => Transition(
      this.name,
      this.consumes.map(pair => (pair._1 - pair._2, pair._2)),
      this.produces.map(pair => (pair._1 + pair._2, pair._2))
    )
    case false => this
  }

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
