package utils.permissions

import models.frontend.DepositFilter
import org.vivaconagua.play2OauthClient.silhouette.User


/** Handles permissions for [[models.frontend.Deposit]] in relation to [[org.vivaconagua.play2OauthClient.silhouette.User]]
 *  So we can reduce the database result for each user by create or extend [[utils.DepositFilter]]
 */
class DepositPermission() {
    
   /** Add restictions to [[utils.DepositFilter]]
    * the function use extend and create
    * @param filter
    * @return
    */
    def restrict(filter: Option[DepositFilter], identity: User) = {
      filter match {
        case Some(f) => extend(f, identity)
        case None => create(identity)
      }
    }
    
    /**
      * Extend restictions to a given DepositFilter
      * @param filter
      * @param identity
      * @return 
      */
    def extend(filter: DepositFilter, identity: User): Option[DepositFilter] = {
      identity.isOnlyVolunteer match {
        case true => identity.getCrew.map(filter.extend( _ ))
        case false => Some(filter)
      }
    }
    
    /**
      * Create a new DepositFilter for User restictions
      * @return
      */
    def create(identity: User): Option[DepositFilter] = {
      identity.isOnlyVolunteer match {
        case true => identity.getCrew.map(ci => DepositFilter(None, None, Some( ci ), None))
        case false => None
      }
    }
    
}
