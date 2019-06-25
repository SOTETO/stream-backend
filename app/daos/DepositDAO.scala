package daos

import java.util.UUID

import daos.exceptions.{DatabaseException, DepositAddException, DepositUnitAddException, DonationNotFoundException}
import javax.inject.{Inject, Singleton}
import play.api.Play
import models.frontend.{Deposit, DepositUnit}
import daos.schema.{DepositTable, DepositUnitTable, DonationTable}
import daos.reader.{DepositReader, DepositUnitReader, DonationReader}
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
  def find(uuid: UUID): Future[Option[Deposit]]
  def create(deposit: Deposit): Future[Either[DatabaseException, Deposit]]
  def update(deposit: Deposit): Future[Option[Deposit]]
  def delete(uuid: UUID): Future[Boolean]
  def all(page: Option[Page], sort: Option[Sort]): Future[List[Deposit]]
  def count : Future[Int]
  def confirm(uuid: UUID, date: Long) : Future[Boolean]
}

@Singleton
class MariaDBDepositDAO @Inject()
  /**
   * Database tables and provider
   */
  (protected val dbConfigProvider: DatabaseConfigProvider) 
  (implicit ec: ExecutionContext) extends DepositDAO with HasDatabaseConfigProvider[JdbcProfile] {

  val donationsTable = TableQuery[DonationTable]
  val depositUnitTable = TableQuery[DepositUnitTable]
  val depositTable = TableQuery[DepositTable]

  import DonationReader._
  import DepositReader._
  import DepositUnitReader._

  /**
    * Creates a joined query instance
    *
    * @author Johann Sell
    * @return {{{ Seq[DepositReader, Option[DepositUnitReader], Option[DonationReader]] }}}
    */
  private def join(page: Option[Page] = None, sort: Option[Sort] = None) = {
    val sortedDeposits = sort.map(s => s.model match {
      case Some(model) if model == "deposit" => depositTable.sortBy(_.sortBy(s).get)
      case None => depositTable.sortBy(_.sortBy(s).get)
      case _ => depositTable
    }).getOrElse(depositTable)
    val pagedDons = page.map(p => sortedDeposits.drop(p.offset).take(p.size)).getOrElse(sortedDeposits)

    val sortedDonations = sort.map(s => s.model match {
      case Some(model) if model == "donation" => donationsTable.sortBy(_.sortBy(s).get)
      case _ => donationsTable
    }).getOrElse(donationsTable)

    for {
      ((deposit, depositUnit), donation) <-
        pagedDons joinLeft
          depositUnitTable on (_.id === _.depositId) joinLeft
          sortedDonations on (_._2.map(_.donationId) === _.id)
    } yield (deposit, depositUnit, donation)
  }

  /**
   * return the Deposit Model via id
   */
  override def find(id: Long): Future[Option[Deposit]] = {
    // action filter deposit Table via id and join all depositUnits
    //run the action and match the Seq
    //if the seq is empty, return None
    //else transform the result Seq via read function to Deposit
    db.run(join(None, None).filter(_._1.id === id).result).map(result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
  }


  /**
   * Implements find via UUID for Deposit Model
   * return the Deposit with all Units
   */
  override def find(uuid: UUID): Future[Option[Deposit]] = {
    //run the action and transform the result Seq via read function to Option[Deposit]
    db.run(join(None, None).filter(_._1.publicId === uuid.toString).result).map( result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
  }

  override def create(deposit: Deposit): Future[Either[DatabaseException, Deposit]] = {
    def unitCreateQuery(unit: DepositUnit, depositId: Long) = (for {
      donationId <- donationsTable.filter(_.public_id === unit.donationId.toString).map(_.id).result
      depositUnit <- if(donationId.nonEmpty) {
        (depositUnitTable returning depositUnitTable.map(_.id)) +=
          DepositUnitReader(unit, depositId, donationId.head)
      } else {
        throw new DonationNotFoundException(unit.donationId)
      }
    } yield depositUnit).transactionally

    val createQuery = (for {
      depositId <- (depositTable returning depositTable.map(_.id)) += DepositReader(deposit)
      _ <- DBIO.sequence(deposit.amount.map(unit => unitCreateQuery(unit, depositId))).transactionally
    } yield depositId).transactionally

    try {
      db.run(createQuery).flatMap(id => find(id).map(_ match {
        case Some(dep) => Right(dep)
        case None => Left(DepositAddException(deposit))
      }))
    } catch {
      case de: DatabaseException => Future.successful(Left(de))
    }
  }

  override def confirm(uuid: UUID, date: Long): Future[Boolean] = {
    db.run(depositTable.filter(_.publicId === uuid.toString).map(_.id).result).flatMap(_.headOption.map(deposit => {
      val qDeposit = for { dep <- depositTable if dep.id === deposit } yield dep.confirmed
      val qDepositUnit = for { unit <- depositUnitTable.filter(_.depositId === deposit) } yield unit.confirmed
      val operations = (for {
        countDeposit <- qDeposit.update(date)
        countDepositUnit <- qDepositUnit.update(date)
      } yield (countDeposit, countDepositUnit)).transactionally

      db.run(operations).map(res => res._1 > 1 && res._2 > 1)
    }).getOrElse(Future.successful(false)))
  }
  override def update(deposit: Deposit): Future[Option[Deposit]] = ???
  override def delete(uuid: UUID): Future[Boolean] = ???

  /**
   * get all deposits
   */
  override def all(page: Option[Page], sort: Option[Sort]): Future[List[Deposit]] = {
    db.run(join(page, sort).result).map( readList( _ ))
  }

  override def count: Future[Int] =
    db.run(join(None, None).groupBy(_._1).size.result)

  /**
   * Transform the Seq[(DepositReader, Option[DepositUnitReader])] to Deposit
   */
  private def read(entries: Seq[(DepositReader, Option[DepositUnitReader], Option[DonationReader])]): Deposit = {
    // group the Seq by DepositUnitReader and map it to List[DepositUnit]
    val amount = entries.groupBy(_._2).toSeq.filter(_._1.isDefined).map(current =>
      current._2.find(c => c._2.isDefined && c._2.get.publicId == current._1.get.publicId)
        .flatMap(_._3.map(donation =>
          current._1.head.toDepositUnit(donation.publicId)
        ))
    ).filter(_.isDefined).map(_.get).toList
    // use the toDeposit function of DepositReader for transform it to Deposit
    entries.groupBy(_._1).toSeq.map(d => d._1.toDeposit(amount)).head
  }
  
  /**
   *  Transform the entries to a list of Deposit
   *  We group the Seq by the Deposit Models and use the read function for transform
    *  Zipping with index is required to preserve the order
   */
  private def readList(entries: Seq[(DepositReader, Option[DepositUnitReader], Option[DonationReader])]): List[Deposit] = {
    entries.zipWithIndex.groupBy(_._1._1).map( grouped =>
      grouped._2.headOption.map(row => (read(grouped._2.map(_._1)), row._2)) // row._2 contains the index that indicates a sort from database
    ).filter(_.isDefined).map(_.get).toList.sortBy(_._2).map(_._1)
  }
}
