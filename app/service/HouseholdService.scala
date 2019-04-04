package services

import daos.HouseholdDAO
import javax.inject.Inject
import models.frontend.Household
import utils.Page

import scala.concurrent.Future

class HouseholdService @Inject() (dao: HouseholdDAO) {
  def count : Future[Int] = dao.count
  def all(page: Page) : Future[List[Household]] = dao.all(page)
  def save(household: Household): Future[Option[Household]] = dao.save(household)
  def update(household: Household): Future[Option[Household]] = dao.update(household)
}