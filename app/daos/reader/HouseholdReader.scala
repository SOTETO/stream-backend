package daos.reader

import models.frontend._
import slick.jdbc.GetResult
import java.util.UUID
import utils.Validate

/**
 * Implements the Database Reader of Household
 *
 * -------------         --------------------
 * | Household | 1 <-- n | HouseholdVersion |
 * -------------         --------------------
 *        1
 *        ^
 *        |
 *        n
 * ----------------
 * | PlaceMessage |
 * ----------------
 * 
 *
 */


case class PlaceMessageReader(
  id: Long,
  token: Int,
  name: String,
  ){
    def toPlaceMessage: PlaceMessage = 
      PlaceMessage(this.name, this.token)
  }

object PlaceMessageReader extends ((Long, Int, String) => PlaceMessageReader) {
  
  def apply(tuple: (Long, Int, String)): PlaceMessageReader =
    PlaceMessageReader( tuple._1, tuple._2, tuple._3)
}

case class HouseholdVersionReader(
  id: Long,
  iban: Option[String],
  bic: Option[String],
  created: Long,
  updated: Long,
  author: Option[String],  //String representation of a UUID
  editor: Option[String],  //String representation of a UUID
  amount: Double,
  currency: String,
  reasonWhat: Option[String],    
  reasonWerefor: Option[String],
  request: Boolean,
  volunteerManager: Option[String], //String representation of a UUID
  employee: Option[String], //String representation of a UUID
  householdId: Long
  ){
    def toHouseholdVersion: HouseholdVersion = {
      val author : Option[UUID] = Validate.optionUUID(this.author)
      HouseholdVersion(
        this.iban,
        this.bic,
        this.created,
        this.updated,
        Validate.optionUUID(this.author),
        Validate.optionUUID(this.editor),
        HouseholdAmount(this.amount, this.currency),
        Reason(this.reasonWhat, this.reasonWerefor),
        request,
        Validate.optionUUID(this.volunteerManager),
        Validate.optionUUID(this.employee)
      )
    }
  }

object HouseholdVersionReader {
  
  def apply(tuple: (Long, Option[String], Option[String], Long, Long, Option[String], Option[String], Double, String, Option[String], Option[String], Boolean, Option[String], Option[String], Long)): HouseholdVersionReader = 
    HouseholdVersionReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, tuple._9, tuple._10, tuple._11, tuple._12, tuple._13, tuple._14, tuple._15)
}

case class HouseholdReader(
  id: Long, 
  publicId: String
  )

object HouseholdReader extends ((Long, String) => HouseholdReader) {
  def apply(tuple: (Long, String)): HouseholdReader = 
    HouseholdReader(tuple._1, tuple._2)
} 
