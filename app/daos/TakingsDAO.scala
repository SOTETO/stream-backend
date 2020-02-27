package daos

import java.util.UUID
import scala.util.{Success, Failure}
import daos.exceptions.TakingAddException
import daos.reader.{DepositUnitReader, TakingReader, InvolvedSupporterReader, SourceReader, InvolvedCrewReader}
import daos.schema.{DepositUnitTable, TakingTable, InvolvedSupporterTable, SourceTable, InvolvedCrewTable, ConfirmedTable}
import javax.inject.{Inject, Singleton}
import models.frontend.{Taking, Source, TakingFilter, Page, Sort, Confirmed}
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}
import org.bouncycastle.cert.ocsp.Req
import models.frontend.DepositUnit
import models.frontend.InvolvedSupporter
import models.frontend.InvolvedCrew
import scala.concurrent.Await
import ch.qos.logback.core.util.Duration._
import scala.concurrent.duration.Duration
import daos.reader.ConfirmedReader

trait TakingsDAO {
  def count(filter: Option[TakingFilter]) : Future[Int]
  def all(page: Option[Page], sort: Option[Sort], filter: Option[TakingFilter]) : Future[List[Taking]]
  def find(uuid: UUID) : Future[Option[Taking]]
  def save(taking: Taking): Future[Either[TakingAddException, Taking]]
  def update(taking: Taking): Future[Either[TakingAddException, Taking]]
  def delete(uuid: UUID): Future[Boolean]
}

@Singleton()
class SQLTakingsDAO @Inject()
  (protected val dbConfigProvider: DatabaseConfigProvider)
  (implicit ec: ExecutionContext) extends TakingsDAO with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
//  import slick.jdbc.MySQLProfile.api._
  type CustomQuery = slick.lifted.Query[
  (daos.schema.TakingTable, 
    slick.lifted.Rep[Option[daos.schema.InvolvedSupporterTable]], 
    slick.lifted.Rep[Option[daos.schema.SourceTable]], 
    slick.lifted.Rep[Option[daos.schema.DepositUnitTable]], 
    slick.lifted.Rep[Option[daos.schema.ConfirmedTable]],
    slick.lifted.Rep[Option[daos.schema.InvolvedCrewTable]]),
  (daos.reader.TakingReader, 
    Option[daos.reader.InvolvedSupporterReader], 
    Option[daos.reader.SourceReader], 
    Option[daos.reader.DepositUnitReader],
    Option[daos.reader.ConfirmedReader],
    Option[daos.reader.InvolvedCrewReader])
  ,Seq]


  type sourceQuery = slick.lifted.Query[daos.schema.SourceTable,daos.schema.SourceTable#TableElementType,Seq]
  val takings = TableQuery[TakingTable]
  val supporter = TableQuery[InvolvedSupporterTable]
  val sources = TableQuery[SourceTable]
  val depositUnits = TableQuery[DepositUnitTable]
  val crews = TableQuery[InvolvedCrewTable]
  val confirms = TableQuery[ConfirmedTable]
  import TakingReader._
  import InvolvedSupporterReader._
  import SourceReader._
  import daos.reader.DepositUnitReader._

  /**
    * Generate a Query for TakingTable
    * @return
    */

    private def joined(): CustomQuery = {
      (for {
        (((((don, sup), sou), units), conf), cre) <- (
          takings joinLeft
          supporter on (_.id === _.taking_id)) joinLeft
          sources on (_._1.id === _.taking_id) joinLeft
          depositUnits on (_._1._1.id === _.takingId) joinLeft
          confirms on (_._2.map(_.depositId) ===_.depositId) joinLeft
          crews on (_._1._1._1._1.id === _.taking_id)
      } yield (don, sup, sou, units, conf, cre))
    }

  /**
   * Sorted Query for Taking
   * @param query
   * @param sort
   * @return CustomQuery
   *
   */
  
  private def getConfirmed(id: Long):Option[Confirmed] = {
   Await.result( db.run(confirms.filter(_.depositId === id).result).map(res => res.isEmpty match {
      case false => Some(res.head.toConfirmed)
      case true => None
    }), Duration.Inf)
  }
  private def sorted(query: CustomQuery, sort: Option[Sort]) = {
    query.sortBy(result => sort match {
        case Some(s) => s.model match {
          case Some(model) if model == "taking" => result._1.sortBy(s).getOrElse(result._1.id.asc)
//        case Some(model) if model == "taking" && s.field == "crew" => takings.sortBy(_.author) // TODO!
          case _ => result._1.id.asc
        }
        case _ => result._1.id.asc
    })
  } 
  
  /**
   * add pagination to taking query
   * @param query
   * @param page
   * @return 
   *
   */

  private def paged(query: CustomQuery, page: Option[Page]) = {
    page.map(p => query.drop(p.offset).take(p.size)).getOrElse(query)
  }
  
  /**
   * add filter to taking query
   * @param query
   * @param filter
   * @return
   */
  private def filtered(query: CustomQuery, filter: Option[TakingFilter]) = {
    filter.map(f => {
      query.filter(table => {
        List(
          f.publicId.map(ids => table._1.public_id === ids.toString()),
          f.name.map(names => names.map(n => table._1.description like n).reduceLeft(_ || _)),
          f.crew.map(crews => table._6.filter(_.crew_id === crews.toString()).isDefined),
          f.crewname.map(crews => crews.map(c => table._6.filter(_.name like c).isDefined).reduceLeft(_ || _)),
          f.payfrom.map(c => table._1.received >= c),
          f.payto.map(c => table._1.received <= c),
          f.crfrom.map(c => table._1.created >= c),
          f.crto.map(c => table._1.created <= c),
          f.norms.map(n => table._3.filter(_.norms === n).isDefined),
          f.external.map(e => if (e == true) {table._3.filter(_.type_of_source === "extern").isDefined} else {table._3.filter(_.type_of_source === "cash").isDefined})

         // f.norms.map(norms => table._1.norms.inSet(norms.map(_.toString())))
        ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true:Rep[Boolean])
      })
    }).getOrElse(query)
  }

  private def read(results: Seq[(TakingReader, Option[InvolvedSupporterReader], Option[SourceReader], Option[DepositUnitReader], Option[ConfirmedReader], Option[InvolvedCrewReader])]): Taking = {
    val publicId: UUID = results.map(r => r._1.publicId).head
    val units: List[DepositUnit] = results.filter(_._4.isDefined).map(r => {
      val confirmed: Option[Confirmed] = r._5.map(_.toConfirmed)
      r._4.map(_.toDepositUnit(publicId, None, confirmed)).get
    }).toList
    val sources: List[Source] = results.map(_._3).filter(_.isDefined).map(_.get.toSource).toList
    val supporters: List[InvolvedSupporter] = results.map(_._2).filter(_.isDefined).map(_.get.toUUID).toList
    val crews: List[InvolvedCrew] = results.map(_._6).filter(_.isDefined).map(_.get.toInvolvedCrew).toList
    
    results.head._1.toTaking(supporters, sources, units, crews)
  }
/**
  * 
  *
  * @param results
  * @return
  */
  private def seqToList(results: Seq[(TakingReader, Option[InvolvedSupporterReader], Option[SourceReader], Option[DepositUnitReader], Option[ConfirmedReader], Option[InvolvedCrewReader])]) : List[Taking] = {
    results.zipWithIndex.groupBy(_._1._1).map( grouped => 
      grouped._2.headOption.map(row => (read(grouped._2.map(_._1)), row._2))
    ).filter(_.isDefined).map(_.get).toList.sortBy(_._2).map(_._1)
  }

  private def find(id: Long): Future[Option[Taking]] =
    db.run(joined().filter(_._1.id === id).result).map(res => seqToList( res ).headOption)
  
  /**
   * count takings models
   * @param filter
   * @return
   */
  override def count(filter: Option[TakingFilter]): Future[Int] = {
    val query = joined()
    db.run(filtered(query, filter).length.result)
  }

  /**
   * get a list of Takings based on params
   * @param page
   * @param sort
   * @param filter
   */

  override def all(page: Option[Page], sort: Option[Sort], filter: Option[TakingFilter]): Future[List[Taking]] = {
    val query = joined()
    val fQuery = filtered(query, filter)
    val sQuery = sorted(fQuery, sort)
    val pQuery = paged(sQuery, page)
    db.run(pQuery.result).map(seqToList( _ ))
  }

  override def find(uuid: UUID): Future[Option[Taking]] =
    db.run(joined().filter(_._1.public_id === uuid.toString).result).map(seqToList( _ ).headOption)

  override def save(taking: Taking): Future[Either[TakingAddException, Taking]] = {
    val insert = (for {
      dID <- (takings returning takings.map(_.id)) += TakingReader(taking)
      _ <- supporter ++= taking.amount.involvedSupporter.map(id => InvolvedSupporterReader( id, dID ))
      _ <- sources ++= taking.amount.sources.map(source => SourceReader(source, dID))
      _ <- crews ++= taking.crew.map(c => InvolvedCrewReader(c, dID))
    } yield dID).transactionally
    db.run(insert).flatMap(id => find(id).map(_ match {
      case Some(don) => Right(don)
      case None => Left(TakingAddException(taking))
    }))
  }
  
  def updateSources(source: List[Source], taking_id: Long) = {
    var action: List[Req] = Nil
    source.foreach { s =>
      s.publicId match {
        case Some(publicId) => {
          db.run(sources.filter(_.public_id === publicId.toString()).map(_.id).result).map(
            source_id =>  db.run(sources.insertOrUpdate(SourceReader(s, source_id.head, taking_id, publicId.toString())))
          )
        }
        case None => db.run(sources returning sources.map(_.id) += SourceReader(s, taking_id))
      }
    }
  }
  override def update(taking: Taking): Future[Either[TakingAddException, Taking]] = {
    db.run(takings.filter(_.public_id === taking.id.toString).map(_.id).result).map( taking_id => {
      updateSources(taking.amount.sources, taking_id.head)
      db.run(takings.insertOrUpdate(TakingReader(taking, Some(taking_id.head))))
    }).flatMap(t => find(taking.id).map(_ match {
      case Some(take) => Right(take)
      case None => Left(TakingAddException(taking)) 
    }))
  }

  override def delete(uuid: UUID): Future[Boolean] = ???
}
