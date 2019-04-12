import daos.{DropsUserDAO, HouseholdDAO, InMemoryHousholdDAO, UserDAO}
import play.api.{Configuration, Environment}
import services.HouseholdService

class StreamModule extends play.api.inject.Module {
  def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[HouseholdDAO].to[InMemoryHousholdDAO],
      bind[UserDAO].to[DropsUserDAO]
    )
  }
}