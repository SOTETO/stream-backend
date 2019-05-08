package utils.petriNet

case class Token(amount: Int) {
  def + (other: Token): Token = Token(this.amount + other.amount)
  def - (other: Token): Token = Token(this.amount - other.amount)
  def <= (other: Token): Boolean = this.amount <= other.amount
  def > (other: Token): Boolean = this.amount > other.amount
  def >= (other: Token): Boolean = this.amount >= other.amount
}
