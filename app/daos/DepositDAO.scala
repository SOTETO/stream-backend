package daos

import java.util.UUID
import javax.inject.Inject
import play.api.Play
import models.frontend.Deposit
import models.database.{ DepositTableDef, DepositUnitTableDef}
import play.api.Configuration
import utils._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._


import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

trait DepositDAO {
//def count (filter: HousholdFilter) : Future[Option[Int]]
// def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter]): Future[Option[List[Deposit]]]
  def find(id: Long): Future[Option[Deposit]]
  def find(uuid: UUID): Future[Option[Deposit]]
  def create(deposit: Deposit): Future[Option[Deposit]]
  def update(deposit: Deposit): Future[Option[Deposit]]
  def delete(uuid: UUID): Future[Boolean]
}

class MariaDBDepositDAO extends DepositDAO {
  /**
   * Database tables and provider
   */
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val depositUnitTable = TableQuery[DepositUnitTableDef]
  val depositTable = TableQuery[DepositTableDef]
  

  def find(id: Long): Future[Option[Deposit]] = {
    val action = for {
      (deposit, depositUnit) <- (depositTable.filter(d => d.id === id) joinLeft depositUnitTable on (_.id === _.depositId))
    } yield (deposit, depositUnit)
    dbConfig.db.run(action.result).flatMap(depositSeq => 
        depositSeq.groupBy(_._1).map(d => d._1.toDeposit(d._2.toList))
        )
  }
  /**
   * Implements find via UUID for Deposit Model
   * return the Deposit with all Units
   */
  def find(uuid: UUID): Future[Option[Deposit]] = ???
   
  /**
   * save deposit into database 
   * used variables:
   *  deposit : Deposit
   *  depositId : Long 
   *  depositUnit : DepositUnit
   *  id : Long
   */
  def create(deposit: Deposit): Future[Option[Deposit]] = {
    //insert deposit into DepositTable
    dbConfig.db.run((depositTable returning depositTable.map(_.id)) += deposit.toDepositDB).map(depositId => {
      //insert depositUnits with depositId as foreignKey 
      deposit.amount.foreach(depositUnit => {
        dbConfig.db.run((depositUnitTable returning depositUnitTable.map(_.id)) += depositUnit.toDepositUnitDB(depositId))
      })
      //return depositId
      depositId
      // return Option[Deposit] via find(id: Long) function
    }).flatMap(id => find(id))
  }

  def update(deposit: Deposit): Future[Option[Deposit]] = ???
  def delete(uuid: UUID): Future[Boolean] = ???
  
}

/*class InMemoryDepositDAO @Inject()(implicit ws: WSClient, config: Configuration) extends DepositDAO with Filter[Deposit, SortDir]{
  
  override val operations: List[FilteringOperation[Deposit, SortDir]] = List(
    FilteringOperation[Deposit, SortDir](
      FilterableField("deposit.what"),
      (dir: SortDir) => (h1: Deposit, h2: Deposit) => {
        def compare(w1: Option[String], w2: Option[String]) : Boolean = dir match {
          case Ascending => w1 match {
            case Some(what1) => w2 match {
              case Some(what2) => what1 <= what2
              case None => false
            }
            case None => true
          }
          case Descending => w1 match {
            case Some(what1) => w2 match {
              case Some(what2) => what1 > what2
              case None => true
            }
            case None => false
          }
        }

        h1.versions.lastOption match {
          case Some(v1) => h2.versions.lastOption match {
            case Some(v2) => compare(v1.reason.what, v2.reason.what)
            case None => compare(v1.reason.what, None)
          }
          case None => h2.versions.lastOption match {
            case Some(v2) => compare(None, v2.reason.what)
            case None => true
          }
        }
      }
    ),
    FilteringOperation[Deposit, SortDir](
      FilterableField("deposit.wherefor"),
      (dir: SortDir) => (h1: Deposit, h2: Deposit) => {
        def compare(w1: Option[String], w2: Option[String]) : Boolean = dir match {
          case Ascending => w1 match {
            case Some(wherefor1) => w2 match {
              case Some(wherefor2) => wherefor1 <= wherefor2
              case None => false
            }
            case None => true
          }
          case Descending => w1 match {
            case Some(wherefor1) => w2 match {
              case Some(wherefor2) => wherefor1 > wherefor2
              case None => true
            }
            case None => false
          }
        }

        h1.versions.lastOption match {
          case Some(v1) => h2.versions.lastOption match {
            case Some(v2) => compare(v1.reason.wherefor, v2.reason.wherefor)
            case None => compare(v1.reason.wherefor, None)
          }
          case None => h2.versions.lastOption match {
            case Some(v2) => compare(None, v2.reason.wherefor)
            case None => true
          }
        }
      }
    ),
    FilteringOperation[Deposit, SortDir](
      FilterableField("deposit.crew"),
      (dir: SortDir) => {
        // Todo: Implement this non-blocking!
        def usersSortedByCrews = Await.result(this.depositEntries.flatMap(deposits =>
          userDAO.sortByCrew(deposits.filter(_.versions.head.author.isDefined).map(_.versions.head.author.get).distinct, dir)
        ), 3000 millis)

        def getIndex(h: Deposit): Option[Int] =
          h.versions.headOption.flatMap(_.author.map(usersSortedByCrews.indexOf(_)).flatMap(_ match {
            case i: Int if i >= 0 => Some(i)
            case _ => None
          }))

        def compare(i1: Option[Int], i2: Option[Int]): Boolean = i1 match {
          case Some(i) => i2 match {
            case Some(j) => dir match {
              case Ascending => i <= j
              case Descending => i > j
            }
            case None => dir match {
              case Ascending => true
              case Descending => false
            }
          }
          case None => dir match {
            case Ascending => false
            case Descending => true
          }
        }

        (h1: Deposit, h2: Deposit) => compare(getIndex(h1), getIndex(h2))
      }
    ),
    FilteringOperation[Deposit, SortDir](
      FilterableField("deposit.amount"),
      (dir: SortDir) => (h1: Deposit, h2: Deposit) =>
        (dir == Ascending && h1.versions.last.amount.amount <= h2.versions.last.amount.amount) ||
          (dir == Descending && h1.versions.last.amount.amount > h2.versions.last.amount.amount)
    ),
    FilteringOperation[Deposit, SortDir](
      FilterableField("deposit.created"),
      (dir: SortDir) => (h1: Deposit, h2: Deposit) =>
        (dir == Ascending && h1.versions.head.created <= h2.versions.head.created) ||
          (dir == Descending && h1.versions.head.created > h2.versions.head.created)
    ),
    FilteringOperation[Deposit, SortDir](
      FilterableField("deposit.updated"),
      (dir: SortDir) => (h1: Deposit, h2: Deposit) =>
        (dir == Ascending && h1.versions.last.updated <= h2.versions.last.updated) ||
          (dir == Descending && h1.versions.last.updated > h2.versions.last.updated)
    )
  )
  
  def count (filter: HousholdFilter) : Future[Option[Int]] = depositEntries.map(
    _.filter(d => filter.map(_ ? d).getOrElse(true)).size
  )
  def all(page: Option[Page], sort: Option[Sort], filter: Option[DepositFilter]): Future[Option[List[Deposit]]] = {
    this.depositEntries.map(entries => {
      val pagination = page.map(p => (p.offset, p.offset + p.size)).getOrElse((0, entries.length))
      entries.filter(h => filter.map(_ ? h).getOrElse(true))
        .sortWith(sort.map(s =>
          this.operations
            .find(_.field == FilterableField(s.field)).map(_.toSortOperation(s.dir))
            .getOrElse(this.operations.head.toSortOperation(s.dir))
        ).getOrElse((_, _) => true)).slice(pagination._1, pagination._2)
    })

  }

  override def find(uuid: UUID): Future[Option[Household]] = depositEntries.map(_.find(_.id == uuid))

  override def save(deposit: Household): Future[Option[Household]] = {
    depositEntries = depositEntries.map(_ :+ deposit)
    find(deposit.id)
  }

  override def update(deposit: Household): Future[Option[Household]] = {
    depositEntries = depositEntries.map(_.map(h => h.id match {
      case deposit.id => deposit
      case _ => h
    }))
    find(deposit.id)
  }

  override def addVersion(uuid: UUID, version: HouseholdVersion): Future[Option[Household]] =
    depositEntries.map(_.find(_.id == uuid).map(_.addVersion(version)))

  override def delete(uuid: UUID): Future[Boolean] = {
    val count = depositEntries.map(_.size)
    depositEntries = depositEntries.map(_.filter(_.id != uuid))
    count.flatMap(c => depositEntries.map(_.size + 1 == c))
  }

}*/
