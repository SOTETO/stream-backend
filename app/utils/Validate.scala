package utils

import java.util.UUID

object Validate {
  
  def optionUUID(stringOption: Option[String]): Option[UUID] = {
    stringOption match {
      case Some(string) => Some(UUID.fromString(string))
      case None => None
    }
  }

  def optionString(uuidOption: Option[UUID]): Option[String] = {
    uuidOption match {
      case Some(uuid) => Some(uuid.toString)
      case None => None
    }
  }
}
