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
  
  /* filter function for HouseholdVersion
   *  
   */
  private def filterHouseholdVersion(filter: Option[HouseholdFilter]) = {
    filter.map(f => {
      householdVersionTable.filter(table => {
        List(
          f.what.map(table.reasonWhat like _ ++ "%"),
          f.wherefor.map(table.reasonWherefor like _ ++ "%"),
          f.crew.map(table.crewId === _.toString()),
          f.amount.map(table.amount === _)
        ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true:Rep[Boolean])
      })
    }).getOrElse(householdVersionTable)
  }

  private def join(page: Option[Page] = None, sort: Option[Sort] = None, filter: Option[HouseholdFilter]= None) = {
    val filteredHouseholdVersionTable = filterHouseholdVersion(filter)
    val sortedHouseholdVersion = sort.map(s => s.model match {
      case Some(model) if model == "household_version" => filteredHouseholdVersionTable.sortBy(t => t.sortBy(s).getOrElse(t.id.asc))
      //      case Some(model) if model == "donation" && s.field == "crew" => donations.sortBy(_.author) // TODO!
      case None => filteredHouseholdVersionTable.sortBy(t => t.sortBy(s).getOrElse(t.id.asc))
      case _ => filteredHouseholdVersionTable
    }).getOrElse(filteredHouseholdVersionTable)

    
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
    db.run(join(page, sort, filter).result).map(readList( _ ))
  
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


