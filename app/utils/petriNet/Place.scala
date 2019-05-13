package utils.petriNet

/**
  * Represents a Petri Net Place. It consists of a name and token.
  *
  * @author Johann Sell
  * @param name
  * @param token
  */
case class Place(name: String, token: Token) {
  def + (token: Token) : Place = Place(this.name, this.token + token)
  def - (token: Token) : Place = Place(this.name, this.token - token)

  /**
    * Checks if another given place has the same name.
    *
    * @author Johann Sell
    * @param other
    * @return
    */
  def ~ (other: Any): Boolean = other match {
    case p : Place => this.name == p.name
    case _ => false
  }

  def >= (token: Token) : Boolean = this.token >= token

  def > (token: Token) : Boolean = this.token > token

  override def toString: String = "Place('" + name + "' -> " + token.amount + ")"
}

object Place {
  def apply(name: String) : Place = Place(name, Token(0))
}