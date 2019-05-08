package utils.petriNet

case class Place(name: String, token: Token) {
  def + (token: Token) : Place = Place(this.name, this.token + token)
  def - (token: Token) : Place = Place(this.name, this.token - token)

  def ~ (other: Any): Boolean = other match {
    case p : Place => this.name == p.name
    case _ => false
  }

//  def has(token: Token) : Boolean = this.token <= token

  def >= (token: Token) : Boolean = this.token >= token

  def > (token: Token) : Boolean = this.token > token

  override def toString: String = "Place('" + name + "' -> " + token.amount + ")"
}

object Place {
  def apply(name: String) : Place = Place(name, Token(0))
}