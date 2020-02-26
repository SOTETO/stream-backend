package utils

import java.util.UUID
import scala.util.{ Try, Success, Failure}

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
  
  def isUUID(uuidStringOption: Option[String]): Option[UUID] = {
    uuidStringOption match {
      case Some(uuidString) => {
        val uuid = Try(UUID.fromString(uuidString))
        uuid match {
          case Success(v) => Some(v)
          case Failure(e) => None
        }
      }
      case None => None
    }
  }
}
