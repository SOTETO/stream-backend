
package service

import javax.inject.Inject
import play.libs.ws._
import java.util.UUID
import java.util.concurrent.CompletionStage
import play.api.Configuration
import models.frontend.InvolvedSupporter

class DropsRequest @Inject() (ws: WSClient, config: Configuration) {
  
  def createQuery(uuidList: List[UUID]): DropsQuery = {
    
  }

  def getSupporterByList(uuidList: List[UUID]): Option[List[InvolvedSupporter]] = {

    val query = uuidList.foreach(uuid => {
        
    })
    ws.url(config.get[String]("drops.url.userById"))
      .addHeader("Content-Type", "application/xml")
      .get()
  }
}
