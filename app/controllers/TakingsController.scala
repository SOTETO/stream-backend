package controllers

import java.util.UUID
import play.api.mvc._
import scala.concurrent.ExecutionContext
import com.mohiva.play.silhouette.api.Silhouette
import org.vivaconagua.play2OauthClient.silhouette.CookieEnv
import org.vivaconagua.play2OauthClient.silhouette.UserService
import org.vivaconagua.play2OauthClient.drops.authorization._



@Singleton
class TakingsController @Inject()(
    cc: ControllerComponents,
    silhouette: Silhouette[CookieEnv],
    userService: UserServer,
    service: TakingsService
  ) extends AbstractController(cc) {
    implicit val ec = ExecutionContext.global

    def get = silhouette.SecuredAction
  }
