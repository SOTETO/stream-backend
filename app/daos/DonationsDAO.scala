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

  val donations = TableQuery[DonationTable]
  val supporter = TableQuery[InvolvedSupporterTable]
  val sources = TableQuery[SourceTable]
  val depositUnits = TableQuery[DepositUnitTable]
  import DonationReader._
  import InvolvedSupporterReader._
  import SourceReader._
  import daos.reader.DepositUnitReader._

  /**
    * Generates a sorted and paginated join. While sorting can be excuted on all parts of the join, we only want to
    * paginate the {{{ Donation }}}.
    *
    * @author Johann Sell
    * @param page
    * @param sort
    * @return
    */
  private def donationJoin(page: Option[Page] = None, sort: Option[Sort] = None, filter: Option[DonationFilter] = None) = {
    val filteredDons = filter.map(f => {
      donations.filter(table => {
        val nameFilter = f.name.map(n => (t: DonationTable) => t.description like ("%" + n + "%"))
        val idFilter = f.publicId.map(ids => (t: DonationTable) => t.public_id.inSet(ids.map(_.toString)))
        val crewFilter = f.crew.map(crewIds => (t: DonationTable) => t.crew.inSet(crewIds.map(_.toString)))

        val nf = nameFilter.map(f => f(table)).getOrElse(table.id === table.id)
        val idf = idFilter.map(f => f(table)).getOrElse(table.id === table.id)
        val cf = crewFilter.map(f => f(table)).getOrElse(table.id === table.id)

        nf && idf && cf
      })
    }).getOrElse(donations)

    val sortedDons = sort.map(s => s.model match {
      case Some(model) if model == "donation" => filteredDons.sortBy(_.sortBy(s).get)
//      case Some(model) if model == "donation" && s.field == "crew" => donations.sortBy(_.author) // TODO!
      case None => filteredDons.sortBy(_.sortBy(s).get)
      case _ => filteredDons
    }).getOrElse(filteredDons)
    val pagedDons = page.map(p => sortedDons.drop(p.offset).take(p.size)).getOrElse(sortedDons)

    val sortedSources = sort.map(s => s.model match {
      case Some(model) if model == "source" => sources.sortBy(_.sortBy(s).get)
      case _ => sources
    }).getOrElse(sources)

    for {
      (((don, sup), sou), units) <- (
        pagedDons joinLeft
        supporter on (_.id === _.donation_id)) joinLeft
        sortedSources on (_._1.id === _.donation_id) joinLeft
        depositUnits on (_._1._1.id === _.donationId)
    } yield (don, sup, sou, units)
  }

  /**
    * Read a result set of database rows after a join.
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
    db.run(donationJoin(None, None).filter(_._1.id === id).result).map(res => reader( res ).headOption)

  override def count(filter: Option[DonationFilter]): Future[Int] =
    db.run(donationJoin(None, None, filter).groupBy(_._1).size.result)

  override def all(page: Option[Page], sort: Option[Sort], filter: Option[DonationFilter]): Future[List[Donation]] =
    db.run(donationJoin(page, sort, filter).result).map(reader( _ ).toList)

  override def find(uuid: UUID): Future[Option[Donation]] =
    db.run(donationJoin(None, None).filter(_._1.public_id === uuid.toString).result).map(reader( _ ).headOption)

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
