package utils.permissions

import models.frontend.TakingFilter
import org.vivaconagua.play2OauthClient.silhouette.User


/** Handles permissions for [[models.frontend.Taking]] in relation to [[org.vivaconagua.play2OauthClient.silhouette.User]]
 *  So we can reduce the database result for each user by create or extend [[models.frontend.TakingFilter]]
 */
class TakingPermission() {
    
   /** Add restictions to [[models.frontend.TakingFilter]]
    * the function use extend and create
    * @param filter
    * @return
    */
    def restrict(filter: Option[TakingFilter], identity: User) = {
      filter match {
        case Some(f) => extend(f, identity)
        case None => create(identity)
      }
    }
    
    /**
      * Extend restictions to a given taking filter
      * @param filter
      * @param identity
      * @return 
      */
    def extend(filter: TakingFilter, identity: User): Option[TakingFilter] = {
      identity.isOnlyVolunteer match {
        case true => identity.getCrew.map(filter.extend( _ ))
        case false => Some(filter)
      }
    }
    
    /**
      * Create a new taking filter for User restictions
      * @return
      */
    def create(identity: User): Option[TakingFilter] = {
      identity.isOnlyVolunteer match {
        case true => identity.getCrew.map(ci => TakingFilter(None, Some(Set(ci)), None, None))
        case false => None
      }
    }
    
}
