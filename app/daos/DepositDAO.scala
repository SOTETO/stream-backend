package daos

import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.api.Play
import models.frontend.Deposit
import daos.schema.{ DepositTable, DepositUnitTable }
import daos.reader.{DepositReader, DepositUnitReader}
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
  def all(page: Option[Page], sort: Option[Sort]): Future[Option[List[Deposit]]]
}

@Singleton
class MariaDBDepositDAO @Inject()
  /**
   * Database tables and provider
   */
  (protected val dbConfigProvider: DatabaseConfigProvider) 
  (implicit ec: ExecutionContext) extends DepositDAO with HasDatabaseConfigProvider[JdbcProfile] {
  
  val depositUnitTable = TableQuery[DepositUnitTable]
  val depositTable = TableQuery[DepositTable]
  
  /**
   * return the Deposit Model via id
   */
  override def find(id: Long): Future[Option[Deposit]] = {
    // action filter deposit Table via id and join all depositUnits 
    // return Seq[DepositReader, DepositUnitReader]
    val action = for {
      (deposit, depositUnit) <- (depositTable.filter(d => d.id === id) joinLeft depositUnitTable on (_.id === _.depositId))
    } yield (deposit, depositUnit)
    //run the action and match the Seq
    //if the seq is empty, return None
    //else transform the result Seq via read function to Deposit
    db.run(action.result).map(result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
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
    db.run(action.result).map( result => result.isEmpty match {
          case false => Some(read(result))
          case true => None
        })
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
    db.run((depositTable returning depositTable.map(_.id)) += deposit.toDepositReader).map(depositId => {
      //insert depositUnits with depositId as foreignKey 
      deposit.amount.foreach(depositUnit => {
        db.run((depositUnitTable returning depositUnitTable.map(_.id)) += depositUnit.toDepositUnitReader(depositId))
      })
      //return depositId
      depositId
      // return Option[Deposit] via find(id: Long) function
    }).flatMap(id => find(id))
  }

  override def update(deposit: Deposit): Future[Option[Deposit]] = ???
  override def delete(uuid: UUID): Future[Boolean] = ???

  /**
   * get all deposits
   */
  override def all(page: Option[Page], sort: Option[Sort]): Future[Option[List[Deposit]]] = {
    val action = for {
      (deposit, depositUnit) <- (depositTable joinLeft depositUnitTable on (_.id === _.depositId))
    } yield (deposit, depositUnit)
    db.run(action.result).map( result =>
      result.isEmpty match {
        case false => Some(readList(result))
        case true => None
      }
    )
  }


  /**
   * Transform the Seq[(DepositReader, Option[DepositUnitReader])] to Deposit
   */
  private def read(entries: Seq[(DepositReader, Option[DepositUnitReader])]): Deposit = {
    // group the Seq by DepositUnitReader and map it to List[DepositUnit]
    val amount = entries.groupBy(_._2).toSeq.filter(_._1.isDefined).map(current => current._1.head.toDepositUnit).toList
    // use the toDeposit function of DepositReader for transform it to Deposit
    entries.groupBy(_._1).toSeq.map(d => d._1.toDeposit(amount)).head
  }
  
  /**
   *  Transform the entries to a list of Deposit
   *  We group the Seq by the Deposit Models and use the read function for transform
   */
  private def readList(entries: Seq[(DepositReader, Option[DepositUnitReader])]): List[Deposit] = {
    entries.groupBy(_._1).map( grouped => 
      read(grouped._2)
    ).toList
  }
}
