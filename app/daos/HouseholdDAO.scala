package daos

import java.util.UUID

import javax.inject.Inject
import models.frontend.{Household, HouseholdVersion}
import play.api.Configuration
import play.api.libs.ws.WSClient
import utils.Page

import scala.concurrent.{ExecutionContext, Future}

trait HouseholdDAO {
  def count : Future[Int]
  def all(page: Page) : Future[List[Household]]
  def find(uuid: UUID) : Future[Option[Household]]
  def save(household: Household): Future[Option[Household]]
  def update(household: Household): Future[Option[Household]]
  def addVersion(uuid: UUID, version: HouseholdVersion): Future[Option[Household]]
  def delete(uuid: UUID): Future[Boolean]
}

class InMemoryHousholdDAO @Inject()(implicit ws: WSClient, config: Configuration) extends HouseholdDAO {
  implicit val ec = ExecutionContext.global

  var householdEntries : Future[List[Household]] = Household.initTestData(20, config)

  override def count: Future[Int] = householdEntries.map(_.size)

  override def all(page: Page): Future[List[Household]] = this.householdEntries.map(
    _.slice(page.offset, page.offset + page.size)
  )

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
}