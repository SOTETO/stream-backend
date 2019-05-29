package testdata

import java.util.UUID

import daos.UserDAO
import models.frontend._
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

case class DonationTestData(config: Configuration)(implicit userDAO: UserDAO) extends TestData[Donation] {

  object ContextTestData extends TestData[Context] {
    override def init(count: Int): Future[List[Context]] = {
      val r = scala.util.Random
      val descriptions = config.get[Seq[String]]("testData.context.description")
      val categories = config.get[Seq[String]]("testData.context.categories")

      Future.successful((0 to count).foldLeft[List[Context]](Nil)((acc, i) =>
        acc :+ Context(descriptions(r.nextInt(descriptions.size)), categories(r.nextInt(categories.size)))
      ))
    }
  }

  object SourceTestData extends TestData[Source] {
    override def init(count: Int): Future[List[Source]] = {
      val r = scala.util.Random
      val types = config.get[Seq[String]]("testData.donation.source.type")
      val categories = config.get[Seq[String]]("testDate.donation.source.category")
      val currencies = config.get[Seq[String]]("testData.currency")

      Future.successful((0 to count).foldLeft[List[Source]](Nil)((acc, i) =>
        acc :+ Source(
          categories(r.nextInt(categories.size)),
          r.nextDouble(),
          currencies(r.nextInt(currencies.size)),
          types(r.nextInt(types.size))
        )
      ))
    }
  }

  object DonationAmountTestData extends TestData[DonationAmount] {
    override def init(count: Int): Future[List[DonationAmount]] = {
      val r = scala.util.Random

      implicit val ec = ExecutionContext.global

      (0 to count).foldLeft[Future[List[DonationAmount]]](Future.successful(Nil))(
        (acc, c) => acc.flatMap(current =>
          userDAO.get(r.nextInt(3)).flatMap(users =>
            SourceTestData.init(r.nextInt(5)).map(sources =>
              current :+ DonationAmount(System.currentTimeMillis(), users, sources)
            )
          )
        )
      )
    }
  }

  override def init(count: Int): Future[List[Donation]] = {
    val r = scala.util.Random

    implicit val ec = ExecutionContext.global

    (0 to count).foldLeft[Future[List[Donation]]](Future.successful(Nil))(
      (acc, c) => acc.flatMap(current =>
        DonationAmountTestData.init(1).flatMap(amount =>
          ContextTestData.init(1).map(context => {
            val author = amount.head.involvedSupporter.headOption
            val details = author.map(a => Details(
              a.toString, r.nextBoolean()
            ))
            val comment = Some("")
            current :+ Donation(
              UUID.randomUUID(),
              amount.head,
              context.head,
              comment,
              details,
              author.getOrElse(UUID.randomUUID()),
              System.currentTimeMillis(),
              System.currentTimeMillis()
            )
          })
        )
      )
    )
  }
}
