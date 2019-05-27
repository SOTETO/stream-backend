package testdata

import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.Future

trait TestData[T] {
  def init(count: Int): Future[List[T]]
}
