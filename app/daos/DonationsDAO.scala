package daos

import java.util.UUID

import daos.exceptions.DonationAddException
import daos.reader.{DepositUnitReader, DonationReader, InvolvedSupporterReader, SourceReader}
import daos.schema.{DepositUnitTable, DonationTable, InvolvedSupporterTable, SourceTable}
import javax.inject.{Inject, Singleton}
import models.frontend.Donation
import play.api.Play
import utils.{DonationFilter, Page, Sort}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

trait DonationsDAO {
  def count(filter: Option[DonationFilter]) : Future[Int]
  def all(page: Option[Page], sort: Option[Sort], filter: Option[DonationFilter]) : Future[List[Donation]]
  def find(uuid: UUID) : Future[Option[Donation]]
  def save(donation: Donation): Future[Either[DonationAddException, Donation]]
  def update(donation: Donation): Future[Option[Donation]]
  def delete(uuid: UUID): Future[Boolean]
}

@Singleton()
class SQLDonationsDAO @Inject()
  (protected val dbConfigProvider: DatabaseConfigProvider)
  (implicit ec: ExecutionContext) extends DonationsDAO with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
//  import slick.jdbc.MySQLProfile.api._
  type CustomQuery = slick.lifted.Query[(daos.schema.DonationTable, slick.lifted.Rep[Option[daos.schema.InvolvedSupporterTable]], slick.lifted.Rep[Option[daos.schema.SourceTable]], slick.lifted.Rep[Option[daos.schema.DepositUnitTable]]),(daos.reader.DonationReader, Option[daos.reader.InvolvedSupporterReader], Option[daos.reader.SourceReader], Option[daos.reader.DepositUnitReader]),Seq]
  
  val donations = TableQuery[DonationTable]
  val supporter = TableQuery[InvolvedSupporterTable]
  val sources = TableQuery[SourceTable]
  val depositUnits = TableQuery[DepositUnitTable]
  import DonationReader._
  import InvolvedSupporterReader._
  import SourceReader._
  import daos.reader.DepositUnitReader._

  /**
    * Generate a Query for DonationTable
    * @return
    */

    private def joined() = {
      (for {
        (((don, sup), sou), units) <- (
          donations joinLeft
          supporter on (_.id === _.donation_id)) joinLeft
          sources on (_._1.id === _.donation_id) joinLeft
          depositUnits on (_._1._1.id === _.donationId)
      } yield (don, sup, sou, units))
    }

  /**
   * Sorted Query for Donation
   * @param query
   * @param sort
   * @return CustomQuery
   *
   */
  
  private def sorted(query: CustomQuery, sort: Option[Sort]) = {
    query.sortBy(result => sort match {
        case Some(s) => s.model match {
          case Some(model) if model == "donation" => result._1.sortBy(s).getOrElse(result._1.id.asc)
//        case Some(model) if model == "donation" && s.field == "crew" => donations.sortBy(_.author) // TODO!
          case _ => result._1.id.asc
        }
        case _ => result._1.id.asc
    })
  } 
  
  /**
   * add pagination to donation query
   * @param query
   * @param page
   * @return 
   *
   */

  private def paged(query: CustomQuery, page: Option[Page]) = {
    page.map(p => query.drop(p.offset).take(p.size)).getOrElse(query)
  }
  
  /**
   * add filter to donation query
   * @param query
   * @param filter
   * @return
   */
  private def filtered(query: CustomQuery, filter: Option[DonationFilter]) = {
    filter.map(f => {
      query.filter(table => {
        List(
          f.name.map(name => table._1.description like "%" + name +"%"),
          f.publicId.map(ids => table._1.public_id.inSet(ids.map(_.toString()))),
         // f.crew.map(table.crew === _.toString())
          f.norms.map(norms => table._1.norms === norms)
        ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true:Rep[Boolean])
      })
    }).getOrElse(query)
  }

  /**
    * Read a result set of database query.
    *
    * @author Johann Sell
    * @param results
    * @return
    */

  private def reader(results : Seq[(DonationReader, Option[InvolvedSupporterReader], Option[SourceReader], Option[DepositUnitReader])]) : Seq[Donation] = {
    val supporter = results.map(_._2).filter(_.isDefined).map(_.get)
    val sources = results.map(_._3).filter(_.isDefined).map(_.get)
    val units = results.map(_._4).filter(_.isDefined).map(_.get)
    results.map(res =>
      res._1.toDonation(
        supporter.filter(_.donation_id == res._1.id), // involved supporter
        sources.filter(_.donation_id == res._1.id), // sources
        units.filter(_.donationId == res._1.id) // deposit units
      )
    ).distinct
  }

  private def find(id: Long): Future[Option[Donation]] =
    db.run(joined().filter(_._1.id === id).result).map(res => reader( res ).headOption)
  
  /**
   * count donations models
   * @param filter
   * @return
   */
  override def count(filter: Option[DonationFilter]): Future[Int] = {
    val query = joined()
    db.run(filtered(query, filter).length.result)
  }

  /**
   * get a list of Donations based on params
   * @param page
   * @param sort
   * @param filter
   */

  override def all(page: Option[Page], sort: Option[Sort], filter: Option[DonationFilter]): Future[List[Donation]] = {
    val query = joined()
    val fQuery = filtered(query, filter)
    val pQuery = paged(fQuery, page)
    val sQuery = sorted(pQuery, sort)
    db.run(sQuery.result).map(reader( _ ).toList)
  }
  override def find(uuid: UUID): Future[Option[Donation]] =
    db.run(joined().filter(_._1.public_id === uuid.toString).result).map(reader( _ ).headOption)

  override def save(donation: Donation): Future[Either[DonationAddException, Donation]] = {
    val insert = (for {
      dID <- (donations returning donations.map(_.id)) += DonationReader(donation)
      _ <- supporter ++= donation.amount.involvedSupporter.map(id => InvolvedSupporterReader( id, dID ))
      _ <- sources ++= donation.amount.sources.map(source => SourceReader(source, dID))
    } yield dID).transactionally
    db.run(insert).flatMap(id => find(id).map(_ match {
      case Some(don) => Right(don)
      case None => Left(DonationAddException(donation))
    }))
  }

  override def update(donation: Donation): Future[Option[Donation]] = ???

  override def delete(uuid: UUID): Future[Boolean] = ???
}
