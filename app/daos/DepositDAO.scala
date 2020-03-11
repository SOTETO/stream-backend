package daos

import java.util.UUID

import daos.exceptions.{DatabaseException, DepositAddException, DepositUnitAddException, TakingNotFoundException}
import javax.inject.{Inject, Singleton}
import play.api.Play
import models.frontend.{Deposit, DepositUnit, Page, Sort, DepositFilter, Confirmed, InvolvedCrew}
import daos.schema.{DepositTable, DepositUnitTable, TakingTable, ConfirmedTable, InvolvedCrewTable}
import daos.reader.{DepositReader, DepositUnitReader, TakingReader, ConfirmedReader, InvolvedCrewReader}
import play.api.Configuration
//import utils._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import models.frontend.InvolvedCrew
import daos.reader.InvolvedCrewReader

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
    slick.lifted.Rep[Option[daos.schema.ConfirmedTable]],
    slick.lifted.Rep[Option[daos.schema.DepositUnitTable]], 
    slick.lifted.Rep[Option[daos.schema.TakingTable]],
    slick.lifted.Rep[Option[daos.schema.InvolvedCrewTable]]),
  (daos.reader.DepositReader, 
    Option[daos.reader.ConfirmedReader],
    Option[daos.reader.DepositUnitReader], 
    Option[daos.reader.TakingReader],
    Option[daos.reader.InvolvedCrewReader]),
  Seq]  
  
  val takingsTable = TableQuery[TakingTable]
  val depositUnitTable = TableQuery[DepositUnitTable]
  val depositTable = TableQuery[DepositTable]
  val confirmedTable = TableQuery[ConfirmedTable]
  val crews = TableQuery[InvolvedCrewTable]
  import TakingReader._
  import DepositReader._
  import DepositUnitReader._


 /**
   * Generate Query for DepositTable
   * @return
   */
  private def joined():DepositQuery = {
    (for {
      ((((deposit, confirmed), depositUnit), taking), crew) <-
        depositTable joinLeft confirmedTable on (_.id === _.depositId) joinLeft 
        depositUnitTable on (_._1.id === _.depositId) joinLeft
        takingsTable on (_._2.map(_.takingId) === _.id) joinLeft
        crews on (_._2.map(_.id) === _.taking_id)
    } yield (deposit, confirmed,  depositUnit, taking, crew))
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
          f.takingsId.map(ids => table._4.filter(_.public_id === ids.toString()).isDefined),
          f.crew.map(ids => table._1.crew === ids.toString()),
          f.crewname.map(crews => crews.map(c => table._1.crewName like c).reduceLeft(_ || _)),
          f.name.map(name => name.map( n => table._4.filter(_.description like n).isDefined).reduceLeft(_ || _)),
          f.afrom.map(d => table._1.fullAmount >= d),
          f.ato.map(d=> table._1.fullAmount <= d),
          f.confirmed.map(c => if (c == true) { table._2.isDefined} else { table._2.isEmpty}),
          f.cby.map(c => table._2.filter(_.userUUID === c.toString()).isDefined),
          f.cfrom.map(c=> table._2.filter(_.dateOfConfirm >= c).isDefined),
          f.cto.map(c => table._2.filter(_.dateOfConfirm <= c).isDefined),
          f.payfrom.map(c => table._1.dateOfDeposit >= c),
          f.payto.map(c => table._1.dateOfDeposit <= c),
          f.crfrom.map(c => table._1.created >= c),
          f.crto.map(c => table._1.created <= c)
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
  

  private def confirmJoin() = { 
    (for {
      (deposit, confirmed) <- depositTable joinLeft confirmedTable on (_.id === _.depositId) 
    } yield (deposit, confirmed))
  }

  /** 
    * Confirm [[models.frontend.Deposit]] by public_id
    * @param uuid
    * @param date
    * @return
    */
  override def confirm(uuid: UUID, date: Long, user: String, name: String): Future[Boolean] = {
    db.run(confirmJoin().filter(_._1.publicId === uuid.toString()).result).flatMap(result => result.isEmpty match {
      case false => result.head._2 match {
        case Some(confirmed) => Future.successful(true)
        case None => db.run(confirmedTable returning confirmedTable.map(_.id) += ConfirmedReader(0, user, name, date, result.head._1.id)).map(id => id >0)
      }
      case true => Future.successful(false)
    })
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
    db.run(pQuery.result).map(readList( _ )) 
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
  private def read(entries: Seq[(DepositReader, Option[ConfirmedReader], Option[DepositUnitReader], Option[TakingReader], Option[InvolvedCrewReader])]): Deposit = {
    
    val confirmed: Option[Confirmed] = entries.map( c => c._2 match {
      case Some(co) => Some(co.toConfirmed)
      case None => None
    }).head
    val crews: List[InvolvedCrew] = entries.groupBy(_._5).filter(_._1.isDefined).map(entry => entry._1.map(crew => InvolvedCrew(crew.crew_id, crew.name)).get).toList

    // group the Seq by DepositUnitReader and map it to List[DepositUnit]
    val amount = entries.groupBy(_._3).toSeq.filter(_._1.isDefined).map(current =>
      current._2.find(c => c._3.isDefined && c._3.get.publicId == current._1.get.publicId)
        .flatMap(_._4.map(taking =>
          current._1.head.toDepositUnit(taking.publicId, Some(taking.description), confirmed)
        ))
    ).filter(_.isDefined).map(_.get).toList

    // use the toDeposit function of DepositReader for transform it to Deposit
    entries.groupBy(_._1).toSeq.map(d => d._1.toDeposit(amount, confirmed, crews)).head
  }
  
  /**
    *  Transform the entries to a [[scala.collection.immutable.List]] of [[models.frontend.Deposit]]
    *  We group the [[scala.collection.Seq]] by the [[models.frontend.Deposit]] and use the `read` function for transform
    *  Zipping with index is required to preserve the order
    */
  private def readList(entries: Seq[(DepositReader, Option[ConfirmedReader], Option[DepositUnitReader], Option[TakingReader], Option[InvolvedCrewReader])]): List[Deposit] = {
    entries.zipWithIndex.groupBy(_._1._1).map( grouped =>
      grouped._2.headOption.map(row => (read(grouped._2.map(_._1)), row._2)) // row._2 contains the index that indicates a sort from database
    ).filter(_.isDefined).map(_.get).toList.sortBy(_._2).map(_._1)
  }
}
