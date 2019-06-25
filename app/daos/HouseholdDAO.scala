package daos

import java.util.UUID

import javax.inject.Inject
import models.frontend.{Household, HouseholdVersion}
import play.api.Configuration
import play.api.libs.ws.WSClient
//import testdata.HouseholdTestData
import utils._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

trait HouseholdDAO {
  def count(filter: Option[HouseholdFilter]) : Future[Int]
  def all(page: Option[Page], sort: Option[Sort], filter: Option[HouseholdFilter]) : Future[List[Household]]
  def find(uuid: UUID) : Future[Option[Household]]
  def save(household: Household): Future[Option[Household]]
  def update(household: Household): Future[Option[Household]]
  def addVersion(uuid: UUID, version: HouseholdVersion): Future[Option[Household]]
  def delete(uuid: UUID): Future[Boolean]
}

class SQLHouseholdDAO @Inject()(userDAO: UserDAO) extends HouseholdDAO {
  def count(filter: Option[HouseholdFilter]) : Future[Int] = ???
  def all(page: Option[Page], sort: Option[Sort], filter: Option[HouseholdFilter]) : Future[List[Household]] = ???
  def find(uuid: UUID) : Future[Option[Household]] = ???
  def save(household: Household): Future[Option[Household]] = ???
  def update(household: Household): Future[Option[Household]] = ???
  def addVersion(uuid: UUID, version: HouseholdVersion): Future[Option[Household]] = ???
  def delete(uuid: UUID): Future[Boolean] = ???
 
}

/*class InMemoryHousholdDAO @Inject()(implicit ws: WSClient, config: Configuration, userDAO: UserDAO) extends HouseholdDAO with Filter[Household, SortDir] {
  implicit val ec = ExecutionContext.global*/

  //var householdEntries : Future[List[Household]] = HouseholdTestData(config).init(20)

  /**
    * Implements list of {{{FilteringOperation}}} for {{{HouseholdDAO}}}
    */
 /* override val operations: List[FilteringOperation[Household, SortDir]] = List(
    FilteringOperation[Household, SortDir](
      FilterableField("household.what"),
      (dir: SortDir) => (h1: Household, h2: Household) => {
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
    FilteringOperation[Household, SortDir](
      FilterableField("household.wherefor"),
      (dir: SortDir) => (h1: Household, h2: Household) => {
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
    FilteringOperation[Household, SortDir](
      FilterableField("household.crew"),
      (dir: SortDir) => {
        // Todo: Implement this non-blocking!
        def usersSortedByCrews = Await.result(this.householdEntries.flatMap(households =>
          userDAO.sortByCrew(households.filter(_.versions.head.author.isDefined).map(_.versions.head.author.get).distinct, dir)
        ), 3000 millis)

        def getIndex(h: Household): Option[Int] =
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

        (h1: Household, h2: Household) => compare(getIndex(h1), getIndex(h2))
      }
    ),
    FilteringOperation[Household, SortDir](
      FilterableField("household.amount"),
      (dir: SortDir) => (h1: Household, h2: Household) =>
        (dir == Ascending && h1.versions.last.amount.amount <= h2.versions.last.amount.amount) ||
          (dir == Descending && h1.versions.last.amount.amount > h2.versions.last.amount.amount)
    ),
    FilteringOperation[Household, SortDir](
      FilterableField("household.created"),
      (dir: SortDir) => (h1: Household, h2: Household) =>
        (dir == Ascending && h1.versions.head.created <= h2.versions.head.created) ||
          (dir == Descending && h1.versions.head.created > h2.versions.head.created)
    ),
    FilteringOperation[Household, SortDir](
      FilterableField("household.updated"),
      (dir: SortDir) => (h1: Household, h2: Household) =>
        (dir == Ascending && h1.versions.last.updated <= h2.versions.last.updated) ||
          (dir == Descending && h1.versions.last.updated > h2.versions.last.updated)
    )
  )*/
  /*
  override def count(filter: Option[HouseholdFilter]): Future[Int] = householdEntries.map(
    _.filter(h => filter.map(_ ? h).getOrElse(true)).size
  )

  override def all(page: Option[Page], sort: Option[Sort], filter: Option[HouseholdFilter]): Future[List[Household]] = {
    this.householdEntries.map(entries => {
      val pagination = page.map(p => (p.offset, p.offset + p.size)).getOrElse((0, entries.length))
      entries.filter(h => filter.map(_ ? h).getOrElse(true))
        .sortWith(sort.map(s =>
          this.operations
            .find(_.field == FilterableField(s.field)).map(_.toSortOperation(s.dir))
            .getOrElse(this.operations.head.toSortOperation(s.dir))
        ).getOrElse((_, _) => true)).slice(pagination._1, pagination._2)
    })
  }

  override def find(uuid: UUID): Future[Option[Household]] = householdEntries.map(_.find(_.id == uuid))

  override def save(household: Household): Future[Option[Household]] = {
    householdEntries = householdEntries.map(_ :+ household)
    find(household.id)
  }

  override def update(household: Household): Future[Option[Household]] = {
    householdEntries = householdEntries.map(_.map(h => h.id match {
      case household.id => household
      case _ => h
    }))
    find(household.id)
  }

  override def addVersion(uuid: UUID, version: HouseholdVersion): Future[Option[Household]] =
    householdEntries.map(_.find(_.id == uuid).map(_.addVersion(version)))

  override def delete(uuid: UUID): Future[Boolean] = {
    val count = householdEntries.map(_.size)
    householdEntries = householdEntries.map(_.filter(_.id != uuid))
    count.flatMap(c => householdEntries.map(_.size + 1 == c))
  }
}*/
