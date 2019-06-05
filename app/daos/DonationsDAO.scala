package daos

import java.util.UUID

import daos.exceptions.DonationAddException
import daos.reader.{DonationReader, InvolvedSupporterReader, SourceReader}
import daos.schema.{DonationTable, InvolvedSupporterTable, SourceTable}
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
  import DonationReader._
  import InvolvedSupporterReader._
  import SourceReader._

  /**
    * Generates a sorted and paginated join. While sorting can be excuted on all parts of the join, we only want to
    * paginate the {{{ Donation }}}.
    *
    * @author Johann Sell
    * @param page
    * @param sort
    * @return
    */
  private def donationJoin(page: Option[Page] = None, sort: Option[Sort] = None) = {
    val sortedDons = sort.map(s => s.model match {
      case Some(model) if model == "donation" => donations.sortBy(_.sortBy(s).get)
//      case Some(model) if model == "donation" && s.field == "crew" => donations.sortBy(_.author) // TODO!
      case None => donations.sortBy(_.sortBy(s).get)
      case _ => donations
    }).getOrElse(donations)
    val pagedDons = page.map(p => sortedDons.drop(p.offset).take(p.size)).getOrElse(sortedDons)

    val sortedSources = sort.map(s => s.model match {
      case Some(model) if model == "source" => sources.sortBy(_.sortBy(s).get)
      case _ => sources
    }).getOrElse(sources)

    for {
      ((don, sup), sou) <- (pagedDons joinLeft supporter on (_.id === _.donation_id)) joinLeft sortedSources on (_._1.id === _.donation_id)
    } yield (don, sup, sou)
  }

  /**
    * Read a result set of database rows after a join.
    *
    * @author Johann Sell
    * @param results
    * @return
    */
  private def reader(results : Seq[(DonationReader, Option[InvolvedSupporterReader], Option[SourceReader])]) : Seq[Donation] = {
    val supporter = results.map(_._2).filter(_.isDefined).map(_.get)
    val sources = results.map(_._3).filter(_.isDefined).map(_.get)
    results.map(res =>
      res._1.toDonation(
        res._1.id.map(did => supporter.filter(_.donation_id == did)).getOrElse(Nil), // involved supporter
        res._1.id.map(did => sources.filter(_.donation_id == did)).getOrElse(Nil) // sources
      )
    ).distinct
  }

  private def find(id: Long): Future[Option[Donation]] =
    db.run(donationJoin(None, None).filter(_._1.id === id).result).map(res => reader( res ).headOption)

  override def count(filter: Option[DonationFilter]): Future[Int] =
    db.run(donations.size.result)

  override def all(page: Option[Page], sort: Option[Sort], filter: Option[DonationFilter]): Future[List[Donation]] =
    db.run(donationJoin(page, sort).result).map(reader( _ ).toList)

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