package services

import daos.HouseholdDAO
import javax.inject.Inject
import models.frontend.Household

import scala.concurrent.Future

class HouseholdService @Inject() (dao: HouseholdDAO) {
  def all : Future[List[Household]] = dao.all
  def save(household: Household): Future[Option[Household]] = dao.save(household)
  def update(household: Household): Future[Option[Household]] = dao.update(household)
}
