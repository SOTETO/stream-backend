package daos

import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.api.Play
import models.frontend.Deposit
import models.database.{ DepositTableDef, DepositUnitTableDef, DepositDB, DepositUnitDB}
import play.api.Configuration
import utils._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait DepositDAO {
//def count (filter: HousholdFilter) : Future[Option[Int]]
// def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter]): Future[Option[List[Deposit]]]
  def find(id: Long): Future[Option[Deposit]]
  def find(uuid: String): Future[Option[Deposit]]
  def create(deposit: Deposit): Future[Option[Deposit]]
  def update(deposit: Deposit): Future[Option[Deposit]]
  def delete(uuid: UUID): Future[Boolean]
  def all(page: Option[Page], sort: Option[Sort]): Future[List[Option[Donation]]]
}

@Singleton
class MariaDBDepositDAO @Inject()
  /**
   * Database tables and provider
   */
  (protected val dbConfigProvider: DatabaseConfigProvider) 
  (implicit ec: ExecutionContext) extends DepositDAO with HasDatabaseConfigProvider[JdbcProfile] {
  
  val depositUnitTable = TableQuery[DepositUnitTableDef]
  val depositTable = TableQuery[DepositTableDef]
  
  /**
   * return the Deposit Model via id
   */
  override def find(id: Long): Future[Option[Deposit]] = {
    // action filter deposit Table via id and join all depositUnits 
    // return Seq[DepositDB, DepositUnitDB]
    val action = for {
      (deposit, depositUnit) <- (depositTable.filter(d => d.id === id) joinLeft depositUnitTable on (_.id === _.depositId))
    } yield (deposit, depositUnit)
    //run the action and transform the result Seq via read function to Option[Deposit]
    db.run(action.result).map(read(_))
  }


  /**
   * Implements find via UUID for Deposit Model
   * return the Deposit with all Units
   */
  override def find(uuid: String): Future[Option[Deposit]] = {
    val action = for {
      (deposit, depositUnit) <- (depositTable.filter(d => d.publicId === uuid) joinLeft depositUnitTable on (_.id === _.depositId))
    } yield (deposit, depositUnit)
    //run the action and transform the result Seq via read function to Option[Deposit]
    db.run(action.result).map(read(_))
  }
  /**
   * save deposit into database 
   * used variables:
   *  deposit : Deposit
   *  depositId : Long 
   *  depositUnit : DepositUnit
   *  id : Long
   */
  override def create(deposit: Deposit): Future[Option[Deposit]] = {
    //insert deposit into DepositTable
    db.run((depositTable returning depositTable.map(_.id)) += deposit.toDepositDB).map(depositId => {
      //insert depositUnits with depositId as foreignKey 
      deposit.amount.foreach(depositUnit => {
        db.run((depositUnitTable returning depositUnitTable.map(_.id)) += depositUnit.toDepositUnitDB(depositId))
      })
      //return depositId
      depositId
      // return Option[Deposit] via find(id: Long) function
    }).flatMap(id => find(id))
  }

  override def update(deposit: Deposit): Future[Option[Deposit]] = ???
  override def delete(uuid: UUID): Future[Boolean] = ???
  override def all(page: Option[Page], sort: Option[Sort]) = ???
  /**
   * Transform the Seq[(DepositDB, Option[DepositUnitDB])] to Option[Deposit]
   */
  private def read(entries: Seq[(DepositDB, Option[DepositUnitDB])]): Option[Deposit] = {
    // group the Seq by DepositUnitDB and map it to List[DepositUnit]
    val amount = entries.groupBy(_._2).toSeq.filter(_._1.isDefined).map(current => current._1.head.toDepositUnit).toList
    // use the toDeposit function of DepositDB for transform it to Deposit
    entries.groupBy(_._1).toSeq.map(d => d._1.toDeposit(amount)).headOption
  }
}
