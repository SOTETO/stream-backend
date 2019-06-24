package testdata

import java.util.UUID

import daos.UserDAO
import models.frontend._
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

case class HouseholdTestData(config: Configuration)(implicit userDAO: UserDAO) extends TestData[Household] {

  object HouseholdAmountTestData extends TestData[HouseholdAmount] {

    override def init(count: Int): Future[List[HouseholdAmount]] = {
      val r = scala.util.Random
      val currencies = config.get[Seq[String]]("testData.currency")

      Future.successful((0 to count).foldLeft[List[HouseholdAmount]](Nil)((testData, _) => {
        val lowerBound = 5
        val upperBound = 200
        val euro = lowerBound + r.nextInt((upperBound - lowerBound) + 1)
        val cent = BigDecimal(r.nextDouble()).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        testData ++ List(HouseholdAmount(euro + cent, currencies(r.nextInt(currencies.size))))
      }))
    }
  }

  object ReasonTestData extends TestData[Reason] {
    override def init(count: Int): Future[List[Reason]] = {
      val r = scala.util.Random
      val what = config.get[Seq[String]]("testData.household.reason.what")
      val wherefor = config.get[Seq[String]]("testData.household.reason.wherefor")
      Future.successful((0 to count).foldLeft[List[Reason]](Nil)((reasons, i) => reasons ++ List(
        Reason(
          what = r.nextBoolean() || (i > 0 && count % i == 0) match {
            case true => Some(what(r.nextInt(what.size)))
            case _ => None
          },
          wherefor = r.nextBoolean() || (i > 0 && count % i == 0) match {
            case true => Some(wherefor(r.nextInt(wherefor.size)))
            case _ => None
          }
        )
      )))
    }
  }

  object HouseholdVersionTestData extends TestData[HouseholdVersion] {
    override def init(count: Int): Future[List[HouseholdVersion]] = {
      val r = scala.util.Random
      val iban = config.get[Seq[String]]("testData.household.iban")
      val bic = config.get[Seq[String]]("testData.household.bic")
      val created = System.currentTimeMillis()

      implicit val ec = ExecutionContext.global

      userDAO.get(20).flatMap(ul =>
        HouseholdAmountTestData.init(1).flatMap(amount =>
          ReasonTestData.init(1).map(reason =>
            (0 to count).foldLeft[List[HouseholdVersion]](Nil)((versions, i) => versions ++ List(
              HouseholdVersion(
                Some(UUID.randomUUID()),
                iban = r.nextBoolean() match {
                  case true => Some(iban(r.nextInt(iban.size)))
                  case _ => None
                },
                bic = r.nextBoolean() match {
                  case true => Some(bic(r.nextInt(bic.size)))
                  case _ => None
                },
                created = created,
                updated = i == 0 match {
                  case true => created
                  case _ => System.currentTimeMillis()
                },
                amount = amount.head,
                reason = reason.head,
                request = r.nextBoolean(),
                author = i == 0 match {
                  case true => Some(ul(r.nextInt(ul.size)))
                  case _ => None
                },
                editor = i == 0 match {
                  case true => None
                  case _ => Some(ul(r.nextInt(ul.size)))
                },
                volunteerManager = None,
                employee = None
              )
            ))
          )
        )
      )
    }
  }

  override def init(count: Int): Future[List[Household]] = {
    val r = scala.util.Random

    implicit val ec = ExecutionContext.global

    (0 to count).foldLeft[Future[List[Household]]](Future.successful(Nil))((testData, i) =>
      HouseholdVersionTestData.init(r.nextInt(3)).flatMap(versions =>
        testData.map(_ :+ Household(UUID.randomUUID(), PetriNetHouseholdState(versions.lastOption), versions))
      )
    )
  }

}
