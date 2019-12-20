package daos

import java.util.UUID

import daos.exceptions.{DatabaseException, DepositAddException, DepositUnitAddException, TakingNotFoundException}
import javax.inject.{Inject, Singleton}
import play.api.Play
import models.frontend.{Deposit, DepositUnit, Page, Sort, DepositFilter}
import daos.schema.{DepositTable, DepositUnitTable, TakingTable}
import daos.reader.{DepositReader, DepositUnitReader, TakingReader}
import play.api.Configuration
//import utils._
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
  def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter] = None): Future[List[Deposit]]
  def count(filter: Option[DepositFilter] = None) : Future[Int]
  def confirm(uuid: UUID, date: Long, user: String, name: String) : Future[Boolean]
}

@Singleton
class SQLDepositDAO @Inject()
  /**
   * Database tables and provider
   */
  (protected val dbConfigProvider: DatabaseConfigProvider) 
  (implicit ec: ExecutionContext) extends DepositDAO with HasDatabaseConfigProvider[JdbcProfile] {

  
  type DepositQuery = slick.lifted.Query[
  (daos.schema.DepositTable, 
    slick.lifted.Rep[Option[daos.schema.DepositUnitTable]], 
    slick.lifted.Rep[Option[daos.schema.TakingTable]]),
  (daos.reader.DepositReader, 
    Option[daos.reader.DepositUnitReader], 
    Option[daos.reader.TakingReader]),
  Seq]  
  
  val takingsTable = TableQuery[TakingTable]
  val depositUnitTable = TableQuery[DepositUnitTable]
  val depositTable = TableQuery[DepositTable]

  import TakingReader._
  import DepositReader._
  import DepositUnitReader._


 /**
   * Generate Query for DepositTable
   * @return
   */
  private def joined():DepositQuery = {
    (for {
      ((deposit, depositUnit), taking) <-
        depositTable joinLeft 
        depositUnitTable on (_.id === _.depositId) joinLeft
        takingsTable on (_._2.map(_.takingId) === _.id)
    } yield (deposit, depositUnit, taking))
  }

  /**
    * Sort a given [[DepositQuery]] by given [[Sort]]
    * @param query
    * @param sort
    * @return
    */
  private def sorted(query: DepositQuery, sort: Option[Sort]) = {
    query.sortBy(result => sort match {
      case Some(s) => s.model match {
        case Some(model) if model == "deposit" => result._1.sortBy(s).getOrElse(result._1.id.asc)
        case _ => result._1.id.asc
      }
      case _ => result._1.id.asc
    })
  }
  
  /**
    * Set limit and offset for given [[DepositQuery]]
    * @param query
    * @param page
    * @return
    */
  private def paged(query: DepositQuery, page: Option[Page]) = {
    page.map(p => query.drop(p.offset).take(p.size)).getOrElse(query)
  }
  
  /**
    * Filter given [[DepositQuery]] by [[DepositQuery]]
    * provides only AND conditions
    * @param query
    * @param filter
    * @return
    */
  private def filtered(query: DepositQuery, filter: Option[DepositFilter]) = {
    filter.map(f => {
      query.filter(table => {
        List(
          f.publicId.map(ids => table._1.publicId === ids.toString()),
          f.takingsId.map(ids => table._3.filter(_.public_id === ids.toString()).isDefined),
          f.crew.map(ids => table._1.crew === ids.toString())
          //f.norms.map(norms => table._1.norms === norms)
        ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true:Rep[Boolean])
      })
    }).getOrElse(query)
  }

  /**
   * Find user by id. Use `joined` function to create a [[DepositQuery]]
   * and filter by id.
   * @param id
   * @return
   */
  override def find(id: Long): Future[Option[Deposit]] = {
    db.run(joined().filter(_._1.id === id).result).map(result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
  }

 /**
   * Find user by public_id. Use `joined` function to create a [[DepositQuery]]
   * and filter by public_id.
   * @param public_id
   * @return
   */
  override def find(public_id: UUID): Future[Option[Deposit]] = {
    db.run(joined().filter(_._1.publicId === public_id.toString).result).map( result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
  }
  
  /**
    * Create [[daos.schema.DepositTable]] and [[daos.schema.DepositUnitTable]] based on [[models.frontend.Deposit]]
    * and save it into Database
    * @param deposit
    * @return
    */
  override def create(deposit: Deposit): Future[Either[DatabaseException, Deposit]] = {
    def unitCreateQuery(unit: DepositUnit, depositId: Long) = (for {
      takingId <- takingsTable.filter(_.public_id === unit.takingId.toString).map(_.id).result
      depositUnit <- if(takingId.nonEmpty) {
        (depositUnitTable returning depositUnitTable.map(_.id)) +=
          DepositUnitReader(unit, depositId, takingId.head)
      } else {
        throw new TakingNotFoundException(unit.takingId)
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
  
  /** 
    * Confirm [[models.frontend.Deposit]] by public_id
    * @param uuid
    * @param date
    * @return
    */
  override def confirm(uuid: UUID, date: Long, user: String, name: String): Future[Boolean] = {
    db.run(depositTable.filter(_.publicId === uuid.toString).map(_.id).result).flatMap(_.headOption.map(deposit => {
      val qDeposit = for { dep <- depositTable if dep.id === deposit } yield dep.confirmed
      val dUser = for { dep <- depositTable if dep.id === deposit } yield dep.confirmed_user_uuid
      val dName = for { dep <- depositTable if dep.id === deposit } yield dep.confirmed_user_name
      val qDepositUnit = for { unit <- depositUnitTable.filter(_.depositId === deposit) } yield unit.confirmed
      val duUser = for { unit <- depositUnitTable.filter(_.depositId === deposit) } yield unit.confirmed_user_uuid
      val duName = for { unit <- depositUnitTable.filter(_.depositId === deposit) } yield unit.confirmed_user_name
      val operations = (for {
        countDeposit <- qDeposit.update(date)
        userCountDeposit <- dUser.update(user)
        nameCountDeposit <- dName.update(name)
        countDepositUnit <- qDepositUnit.update(date)
        userCountDepositUnit <- duUser.update(user)
        nameCountDepositUnit <- duName.update(name)
      } yield (countDeposit, nameCountDeposit, userCountDeposit, countDepositUnit, userCountDepositUnit, nameCountDepositUnit)).transactionally

    db.run(operations).map(res => res._1 > 1 && res._2 > 1 && res._3 >1 && res._4 > 1 && res._5 > 1 && res._6 > 1)
    }).getOrElse(Future.successful(false)))
  }
  override def update(deposit: Deposit): Future[Option[Deposit]] = ???
  override def delete(uuid: UUID): Future[Boolean] = ???

  /**
   * Get all [[models.frontend.Deposit]]. The function use `joined` for create [[DepositQuery]]. 
   * This Query is than filtered by `filtered` and than sorted by `sorted`.  
   * Last pagination is added by `paged`
   * @param page
   * @param sort
   * @param filter
   */
  override def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter] = None): Future[List[Deposit]] = {
    val query = joined()
    val fQuery = filtered(query, filter)
    val sQuery = sorted(fQuery, sort)
    val pQuery = paged(sQuery, page)
    db.run(sQuery.result).map(readList( _ )) 
  }

  override def count(filter: Option[DepositFilter] = None): Future[Int] = {
    val query = joined()
    db.run(query.length.result)
  }
  /**
    * Transform the `Seq[(DepositReader, Option[DepositUnitReader])]` to `Deposit`
    * @param entries
    * @return
    */
  private def read(entries: Seq[(DepositReader, Option[DepositUnitReader], Option[TakingReader])]): Deposit = {
    // group the Seq by DepositUnitReader and map it to List[DepositUnit]
    val amount = entries.groupBy(_._2).toSeq.filter(_._1.isDefined).map(current =>
      current._2.find(c => c._2.isDefined && c._2.get.publicId == current._1.get.publicId)
        .flatMap(_._3.map(taking =>
          current._1.head.toDepositUnit(taking.publicId, Some(taking.description))
        ))
    ).filter(_.isDefined).map(_.get).toList
    // use the toDeposit function of DepositReader for transform it to Deposit
    entries.groupBy(_._1).toSeq.map(d => d._1.toDeposit(amount)).head
  }
  
  /**
    *  Transform the entries to a [[scala.collection.immutable.List]] of [[models.frontend.Deposit]]
    *  We group the [[scala.collection.Seq]] by the [[models.frontend.Deposit]] and use the `read` function for transform
    *  Zipping with index is required to preserve the order
    */
  private def readList(entries: Seq[(DepositReader, Option[DepositUnitReader], Option[TakingReader])]): List[Deposit] = {
    entries.zipWithIndex.groupBy(_._1._1).map( grouped =>
      grouped._2.headOption.map(row => (read(grouped._2.map(_._1)), row._2)) // row._2 contains the index that indicates a sort from database
    ).filter(_.isDefined).map(_.get).toList.sortBy(_._2).map(_._1)
  }
}
