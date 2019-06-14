package daos.reader

import java.util.UUID
import models.frontend._
import slick.jdbc.GetResult

case class HouseholdVersionReader(
  id: Long,
  iban: Option[String],
  bic: Option[String],
  created: Long,
  updated: Long,
  author: Option[String],  //String representation of a UUID
  editor: Option[String],  //String representation of a UUID
  amount: Double,
  reasonWhat: Option[String],    
  reasonWerefor: Option[String],
  request: Boolean,
  volunteerManager: Option[String], //String representation of a UUID
  employee: Option[String] //String representation of a UUID
  )

object HouseholdVersionReader {
  
  def apply(tuple: (Long, Option[String], Option[String], Long, Long, Option[String], Option[String], Double, Option[String], Option[String], Boolean, Option[String], Option[String])): HouseholdVersionReader = 
    HouseholdVersionReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, tuple._9, tuple._10, tuple._11, tuple._12, tuple._13)
}
