package services

import daos.{HouseholdDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Household
import utils.{HouseholdFilter, Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

class HouseholdService @Inject() (dao: HouseholdDAO, userDAO: UserDAO) {

  implicit val ec = ExecutionContext.global

  private def prepare(filter: Option[HouseholdFilter]): Future[Option[HouseholdFilter]] =
    this.dao.all(None, None, None).flatMap(households => {
      // get ids of all referenced users inside the database!
      val userIds = households.filter(_.versions.head.author.isDefined).map(_.versions.head.author.get).distinct
      // get all user (UUIDs) of the same crew as the given one by the filter
      filter.map(hf => hf.crew match {
        case Some(id) => userDAO.crewSupporter(userIds, id).map(supporter =>
          hf >> supporter
        )
        case _ => Future.successful(hf)
      }) match {
        // Option[Future[...]] to Future[Option[...]]
        case Some(f) => f.map(Some(_))
        case None => Future.successful(None)
      }})

  def count(filter: Option[HouseholdFilter]) : Future[Int] =
    this.prepare(filter).flatMap(hf => dao.count(hf))

  def all(page: Page, sort: Sort, filter: Option[HouseholdFilter]) : Future[List[Household]] =
    this.prepare(filter).flatMap(hf => dao.all(Some(page), Some(sort), hf))

  def save(household: Household): Future[Option[Household]] = {
    dao.save(household).flatMap(option => option match {
      case Some(household) => household.versions.headOption match {
        case Some(version) => version.author match {
          case Some(author) => userDAO.updateDatasource(author).map(_ match {
            case true => Some(household) // successful update
            case false => Some(household) // unsucessful update; roll-back only because potentially missing network connection?!
          })
          case None => Future.successful(Some(household)) // not possible to sort by authors crew, because of missing author
        }
        case None => Future.successful(Some(household)) // not possible to sort by authors crew, because of missing version
      }
      case None => Future.successful(None) // given household has not been saved
    })
  }
  def update(household: Household): Future[Option[Household]] = dao.update(household)
}