package daos

import java.util.UUID

import javax.inject.Inject
import models.frontend.{Household, HouseholdVersion, PetriNetHouseholdState, PlaceMessage}
import daos.schema.{HouseholdTable, HouseholdVersionHistoryTable, HouseholdVersionTable, PlaceMessageTable}
import daos.reader.{HouseholdReader, HouseholdVersionReader, PlaceMessageReader}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.{Rep, TableQuery}
import slick.jdbc.MySQLProfile.api._
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

class SQLHouseholdDAO @Inject()
  (userDAO: UserDAO,
   protected val dbConfigProvider: DatabaseConfigProvider )
  (implicit ec: ExecutionContext) extends HouseholdDAO with HasDatabaseConfigProvider[JdbcProfile]{
  
  val householdTable = TableQuery[HouseholdTable]
  val householdVersionTable = TableQuery[HouseholdVersionTable]
  val householdVersionHistoryTable = TableQuery[HouseholdVersionHistoryTable]
  val placeMessageTable = TableQuery[PlaceMessageTable]

  /* pirvate function
   *
   */
  private def join(page: Option[Page] = None, sort: Option[Sort] = None) = {
    val sortedHouseholdVersion = sort.map(s => s.model match {
      case Some(model) if model == "household_version" => householdVersionTable.sortBy(t => t.sortBy(s).getOrElse(t.id.asc))
      //      case Some(model) if model == "donation" && s.field == "crew" => donations.sortBy(_.author) // TODO!
      case None => householdVersionTable.sortBy(t => t.sortBy(s).getOrElse(t.id.asc))
      case _ => householdVersionTable
    }).getOrElse(householdVersionTable)


    val sortedPlaceMessages = sort.map(s => s.model match {
      case Some(model) if model == "place_message" => placeMessageTable.sortBy(_.sortBy(s).get)
      //      case Some(model) if model == "donation" && s.field == "crew" => donations.sortBy(_.author) // TODO!
      case None => placeMessageTable.sortBy(t => t.sortBy(s).getOrElse(t.id.asc))
      case _ => placeMessageTable
    }).getOrElse(placeMessageTable)

    val pagedHousehold = page.map(p => householdTable.drop(p.offset).take(p.size)).getOrElse(householdTable)

    (for {
      (((household, householdVersion), placeMessage), oldVersions) <- pagedHousehold join
        sortedHouseholdVersion on (_.id === _.householdId) joinLeft
        sortedPlaceMessages on (_._1.id === _.householdId) joinLeft
        householdVersionHistoryTable on (_._1._1.id === _.householdId)
    } yield (household, householdVersion, placeMessage, oldVersions))
      .sortBy(result => sort match {
          // Sorting afterwards required, since joinLefts seem to break the order defined in request
        case Some(s) => s.model match {
          case Some("household_version") => result._2.sortBy(s).getOrElse(result._1.id.asc)
//          case Some("place_message") => result._3.get.sortBy(s).getOrElse(result._1.id.asc) // TODO
          case _ => result._1.id.asc
        }
        case _ => result._1.id.asc
      })
  }

  /** 
   *  Read a Database Seq and return Household model
   *
   */

  private def read(entries: Seq[(HouseholdReader, HouseholdVersionReader, Option[PlaceMessageReader], Option[HouseholdVersionReader])]): Household = {
    /*
     * Create a Set of PlaceMessageReader and 
     */
    val placeMessages: Set[PlaceMessage] = entries.groupBy(_._3).toSeq.map(current =>
     current._1.map(_.toPlaceMessage)).toSet.filter(_.isDefined).map(_.get)
    val householdVersion: List[HouseholdVersion] =
      entries.groupBy(_._2).toSeq
        .sortBy(_._1.id) // this is important since the frontend assumes an ordered list of versions
        .map(current =>
          current._1.toHouseholdVersion
        ).toList

    val oldHouseholdVersion: List[HouseholdVersion] =
      entries.groupBy(_._4).toSeq
        .sortBy(_._1.map(_.id).getOrElse(0L)) // this is important since the frontend assumes an ordered list of versions
        .map(current =>
        current._1.map(_.toHouseholdVersion)
      ).toList.filter(_.isDefined).map(_.get)
    Household(entries.map(seq =>
      UUID.fromString(seq._1.publicId)).head, PetriNetHouseholdState(placeMessages), oldHouseholdVersion ++ householdVersion
    )
  }
  
  private def readList(entries: Seq[(HouseholdReader, HouseholdVersionReader, Option[PlaceMessageReader], Option[HouseholdVersionReader])]): List[Household] = {
    // Zipping with index preserves the order
    entries.zipWithIndex.groupBy(_._1._1).map( grouped => (read(grouped._2.map(_._1)), grouped._2.head._2) ).toList.sortBy(_._2).map(_._1)
  }

  /* public functions
   *
   */
  def count(filter: Option[HouseholdFilter]) : Future[Int] = db.run(householdTable.size.result)
  def all(page: Option[Page], sort: Option[Sort], filter: Option[HouseholdFilter]) : Future[List[Household]] =
    db.run(join(page, sort).result).map(readList( _ ))
  
  def find(uuid: UUID) : Future[Option[Household]] = {
    db.run(join(None, None).filter(_._1.publicId === uuid.toString).result).map(result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
  }

  def find(id: Long) : Future[Option[Household]] = {
    db.run(join(None, None).filter(_._1.id === id).result).map(result => result.isEmpty match {
      case false => Some(read(result))
      case true => None
    })
  }


  def save(household: Household): Future[Option[Household]] = {
    val insert = (for {
      householdId <- (householdTable returning householdTable.map(_.id)) += HouseholdReader(household)
      _ <- placeMessageTable ++= household.state.toMessages.map(pm => PlaceMessageReader(pm, householdId))
      _ <- householdVersionTable += HouseholdVersionReader(household.versions.reverse.head, householdId)
      _ <- householdVersionHistoryTable ++= household.versions.reverse.tail.reverse.map(v => HouseholdVersionReader(v, householdId))
    } yield householdId).transactionally
    db.run(insert).flatMap(id => find(id))
  }

  def update(household: Household): Future[Option[Household]] = {
    val updateQuery = (for {
      // find the household
      householdId <- householdTable.filter(_.publicId === household.id.toString).map(_.id).result //.result.headOption
      // delete the previous state
      _ <- placeMessageTable.filter(_.householdId === householdId.head).delete
      // save the new state
      _ <- placeMessageTable ++= household.state.toMessages.map(pm => PlaceMessageReader(pm, householdId.head))
      // get id of current version
      currentVersion <- householdVersionTable.filter(_.householdId === householdId.head).map(_.publicId).result
      // delete current version - this version is either equivalent to current version (head of versions) or part of the tail of versions
      _ <- householdVersionTable.filter(_.householdId === householdId.head).delete
      // save current version
      _ <- householdVersionTable += HouseholdVersionReader(household.versions.reverse.head, householdId.head)
      // save new versions
      _ <- householdVersionHistoryTable ++= household.versions.reverse.tail.reverse.filter(v =>
        v.publicId.isEmpty || v.publicId.exists(_.toString == currentVersion.head)
      ).map(v => HouseholdVersionReader(v, householdId.head))
    } yield householdId).transactionally

    db.run(updateQuery).flatMap(_.headOption match {
      case Some(id) => find(id)
      case None => Future.successful(None)
    })
  }
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
