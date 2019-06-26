package utils

import java.util.UUID
import play.api.libs.json.Json

case class DepositFilter(crew: Option[Set[UUID]]) {
  def + (filter: DepositFilter): DepositFilter =
    DepositFilter(this.crew match {
      case Some(crewIds) => Some(crewIds ++ filter.crew.getOrElse(Set()))
      case None => filter.crew
    })
}

object DepositFilter {
  implicit val depositFilterFormat = Json.format[DepositFilter]

  def empty : DepositFilter = DepositFilter(None)
  def apply(crewId: UUID) : DepositFilter = DepositFilter(Some(Set(crewId)))
}
