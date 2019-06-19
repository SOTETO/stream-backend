package utils

import java.util.UUID

object Validate {
  
  def optionUUID(stringOption: Option[String]): Option[UUID] = {
    stringOption match {
      case Some(string) => Some(UUID.fromString(string))
      case None => None
    }
  }
}
