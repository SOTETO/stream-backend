package services

import daos.{HouseholdDAO, UserDAO}
import javax.inject.Inject
import models.frontend.Household
import utils.{Page, Sort}

import scala.concurrent.{ExecutionContext, Future}

class HouseholdService @Inject() (dao: HouseholdDAO, userDAO: UserDAO) {

  implicit val ec = ExecutionContext.global

  def count : Future[Int] = dao.count
  def all(page: Page, sort: Sort) : Future[List[Household]] = dao.all(page, sort)
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