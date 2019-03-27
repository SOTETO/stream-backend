package models

import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.Future

trait TestData[T] {
  def initTestData(count: Int, config: Configuration)(implicit ws: WSClient): Future[List[T]]
}
