package utils.petriNet

/**
  * Represents an amount of tokens in a Petri Net.
  *
  * @author Johann Sell
  * @param amount
  */
case class Token(amount: Int) {
  def + (other: Token): Token = Token(this.amount + other.amount)
  def - (other: Token): Token = Token(this.amount - other.amount)
  def <= (other: Token): Boolean = this.amount <= other.amount
  def > (other: Token): Boolean = this.amount > other.amount
  def >= (other: Token): Boolean = this.amount >= other.amount
}
