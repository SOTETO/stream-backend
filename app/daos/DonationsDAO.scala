package daos

import java.util.UUID

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
  def save(donation: Donation): Future[Option[Donation]]
  def update(donation: Donation): Future[Option[Donation]]
  def delete(uuid: UUID): Future[Boolean]
}

@Singleton()
class SQLDonationsDAO @Inject()
  (protected val dbConfigProvider: DatabaseConfigProvider)
  (implicit ec: ExecutionContext) extends DonationsDAO with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val donations = TableQuery[DonationTable]
  val supporter = TableQuery[InvolvedSupporterTable]
  val sources = TableQuery[SourceTable]
  import DonationReader._
  import InvolvedSupporterReader._
  import SourceReader._

  val donationJoin = for {
    ((don, sup), sou) <- donations joinLeft supporter on (_.id === _.donation_id) joinLeft sources on (_._1.id === _.donation_id)
  } yield (don, sup, sou)

  private def find(id: Long): Future[Option[Donation]] =
    db.run(donationJoin.filter(_._1.id === id).result.headOption).map(_.map(res =>
      res._1.toDonation(
        res._2.map(sup => List(sup.toUUID)).getOrElse(Nil), // involved supporter
        res._3.map(source => List( source.toSource )).getOrElse(Nil) // sources
      )
    ))

  override def count(filter: Option[DonationFilter]): Future[Int] =
    db.run(donations.size.result)

  override def all(page: Option[Page], sort: Option[Sort], filter: Option[DonationFilter]): Future[List[Donation]] =
    db.run(donationJoin.result).map(_.map(res =>
      res._1.toDonation(
        res._2.map(sup => List(sup.toUUID)).getOrElse(Nil), // involved supporter
        res._3.map(source => List( source.toSource )).getOrElse(Nil) // sources
      )
    ).toList)

  override def find(uuid: UUID): Future[Option[Donation]] =
    db.run(donationJoin.filter(_._1.public_id === uuid.toString).result.headOption).map(_.map(res =>
      res._1.toDonation(
        res._2.map(sup => List(sup.toUUID)).getOrElse(Nil), // involved supporter
        res._3.map(source => List( source.toSource )).getOrElse(Nil) // sources
      )
    ))

  override def save(donation: Donation): Future[Option[Donation]] = {
    val insert = (for {
      dID <- (donations returning donations.map(_.id)) += DonationReader(donation)
      _ <- supporter ++= donation.amount.involvedSupporter.map(id => InvolvedSupporterReader( id, dID ))
      _ <- sources ++= donation.amount.sources.map(source => SourceReader(source, dID))
    } yield dID).transactionally
    db.run(insert).flatMap(id => find(id))
  }

  override def update(donation: Donation): Future[Option[Donation]] = ???

  override def delete(uuid: UUID): Future[Boolean] = ???
}