package daos.reader

import models.frontend.{Deposit, DepositUnit, Amount, Confirmed, InvolvedSupporter}
import java.util.UUID

import slick.jdbc.GetResult
import models.frontend.InvolvedCrew





/**
 * A database reader for [[models.frontend.DepositUnit]]
 * @param id internal database id
 * @param publicId stringified [[java.util.UUID]]
 * @param confirmed if Deposit is confiremd, it contains a date as long
 * @param amount amount of [[models.frontend.Amount]]
 * @param currency currency of [[models.frontend.Amount]]
 * @param created date as Long
 * @param depositId foreign key for deposit table
 * @param takingId foreign key for taking table
 */
case class DepositUnitReader(
  id: Long,
  publicId: String,
  amount: Double,
  currency: String,
  created: Long,
  depositId: Long,
  takingId: Long
  ) {
    /**
     * map [[DepositUnitReader]] to [[models.frontend.DepositUnit]]
     * @param takingId
     */
    def toDepositUnit(takingId: UUID, description: Option[String], confirmed: Option[Confirmed]) : DepositUnit = {
      DepositUnit(UUID.fromString(this.publicId), takingId, description, confirmed, Amount(this.amount, this.currency), this.created)
    }
  }
/** Factory for [[DepositUnitReader]] instance*/
object DepositUnitReader extends ((Long, String, Double, String, Long, Long, Long) => DepositUnitReader){
  /** Creates a [[DepositUnitReader]] with given deposit unit, deposit id, taking id and optional id
   *  @param depositUnit deposit unit 
   *  @param depositId foreign key for deposit table
   *  @param takingId foreign key for taking table
   *  @param id can be set for update else the database store the model with a new id
   */
  def apply(depositUnit: DepositUnit, depositId: Long, takingId: Long, id : Option[Long] = None) : DepositUnitReader = {
        DepositUnitReader(
          id.getOrElse(0L),
          depositUnit.publicId.toString,
          depositUnit.amount.amount,
          depositUnit.amount.currency,
          depositUnit.created,
          depositId,
          takingId
        )
  }
  implicit val getDepositUnitReader = GetResult(r =>
    DepositUnitReader(r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextLong, r.nextLong, r.nextLong)
  )
}



/**
 * A database reader for [[models.frontend.Deposit]]
 * @param id database id
 * @param publicId business model id
 * @param fullAmount amount of deposit 
 * @param currency euro dollar
 * @param confirmed can contain a UTC-Date to confirme the deposit
 * @param crew public_id of a crew as String
 * @param supporter public_id of a user as String
 * @param created UTC-Date
 * @param updated UTC-Date
 * @param dateOfDeposit UTC-Date
 */

case class DepositReader(
  id: Long,
  publicId: String,
  fullAmount: Double,
  currency: String,
  crew: String,
  crewName: String,
  supporter: String,
  supporter_name: String,
  created: Long,
  updated: Long,
  dateOfDeposit: Long,
  ) {
    /**
     * map [[DepositReader]] to [[models.frontend.Deposit]]
     * @param depositUnitList list of deposit units
     */
    def toDeposit(depositUnitList: List[DepositUnit], confirmed: Option[Confirmed], crews: List[InvolvedCrew]) = {
      Deposit(
        UUID.fromString(this.publicId),
        Amount(this.fullAmount, this.currency),
        depositUnitList,
        confirmed,
        crews,
        InvolvedSupporter(UUID.fromString(this.supporter), this.supporter_name),
        this.created,
        this.updated,
        this.dateOfDeposit
      )
    }
  }
/** Factory for [[DepositReader]] instance.*/
object DepositReader extends ((Long, String, Double, String, String, String, String, String, Long, Long, Long) => DepositReader){
  /** Create a [[DepositReader]] with given deposit.
   *  @param deposit
   *  @param id can be set for update Deposit, else the database will create a new id
   */
  def apply(deposit: Deposit, id: Option[Long] = None): DepositReader =
        DepositReader(
          id.getOrElse(0L),
          deposit.publicId.toString,
          deposit.full.amount,
          deposit.full.currency,
          "",
          "",
          deposit.supporter.uuid.toString,
          deposit.supporter.name,
          deposit.created,
          deposit.updated,
          deposit.dateOfDeposit
        )
  implicit val getDepositReader = GetResult(r =>
    DepositReader(r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextLong, r.nextLong, r.nextLong)
  )
}


