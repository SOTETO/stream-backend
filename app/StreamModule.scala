import daos._
import play.api.{Configuration, Environment}
//import services.HouseholdService

class StreamModule extends play.api.inject.Module {
  def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[DepositDAO].to[MariaDBDepositDAO],
      bind[HouseholdDAO].to[SQLHouseholdDAO],
      bind[DonationsDAO].to[SQLDonationsDAO],
      bind[UserDAO].to[DropsUserDAO]
    )
  }
}
