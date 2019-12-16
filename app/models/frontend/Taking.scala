package models.frontend

import java.util.UUID

import play.api.Configuration
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient

import scala.concurrent.Future


/** A class for handling details for takings
 * @param reasonForPayment
 * @param receipt
 */
case class Details(reasonForPayment: String, receipt: Boolean) // Todo: Optional Partner
/** Factory for [[Details]] instance. Can be handle as a json. */
object Details {
  implicit val detailsFormat = Json.format[Details]
}

/** A class for handling takings context 
 * @param description 
 * @param category 
 */
case class Context(description: String, category: String)
/**Factory for [[Context]] instance. Can be handle as a json */
object Context {
  implicit val contextFormat = Json.format[Context]
}

case class ExternalTransaction (
  location: String,
  contactPerson: String,
  email: String,
  address: String,
  receipt: Boolean
  )

object ExternalTransaction {
  implicit val externalTransactionFormat = Json.format[ExternalTransaction]
}

case class TypeOfSource(
  category: String,
  external: Option[ExternalTransaction]
  )

object TypeOfSource{
  implicit val TypeOfSource = Json.format[TypeOfSource]
}

/** A class for handling takings sources 
 * @param category
 * @param amount
 * @param currency
 * @param typeOfSource
 */
case class Source(publicId: Option[UUID], category: String, description: Option[String], amount: Amount, typeOfSource: TypeOfSource, norms: String)
/**Factory for [[Source]] instance. Can be handle as a json.*/
object Source {
  implicit val sourceFormat = Json.format[Source]
}

/** A class for handling the amount of an taking
 * @param received UTC date
 * @param involvedSupporter
 * @param sources
 */

case class InvolvedSupporter(uuid: UUID, name: String)
object InvolvedSupporter {
  implicit val involvedSupporterFormat = Json.format[InvolvedSupporter]
}

case class TakingAmount(received: Long, involvedSupporter: List[InvolvedSupporter], sources: List[Source])
/** Factory for [[TakingAmount]] instance. Can be handle as json.*/
object TakingAmount {
  implicit val takingAmountFormat = Json.format[TakingAmount]
}

/** A class for handling takings business model
 *  @param id 
 *  @param amount
 *  @param context
 *  @param comment
 *  @param details
 *  @param depositUnits
 *  @param author
 *  @param crew 
 *  @param created UTC date
 *  @param updated UTC date
 */
case class TakingStub(
                   amount: TakingAmount,
                   context: Context,
                   comment: Option[String],
                   details: Option[Details],
                   depositUnits: List[DepositUnit],
                   author: UUID,
                   crew: List[InvolvedCrew],
                   created: Long,
                   updated: Long
                   ) {
  def toTaking(): Taking = 
    Taking(UUID.randomUUID(), this.amount, this.context, this.comment, this.details, this.depositUnits, this.author, this.crew, this.created, this.updated)
                   }

/**Factory for [[Taking]] instance. Can be handle as a json.*/
object TakingStub {
  implicit val takingFormat = Json.format[TakingStub]
}

case class Taking(
                   id: UUID,
                   amount: TakingAmount,
                   context: Context,
                   comment: Option[String],
                   details: Option[Details],
                   depositUnits: List[DepositUnit],
                   author: UUID,
                   crew: List[InvolvedCrew],
                   created: Long,
                   updated: Long
                   )

/**Factory for [[Taking]] instance. Can be handle as a json.*/
object Taking {
  implicit val takingFormat = Json.format[Taking]
}

/** A class for handling takings filter object
 * @param publicId
 * @param crew
 * @param name
 * @param norms
 */
case class TakingFilter(
                         publicId: Option[Set[UUID]], // content of the set has to be concatenated by OR
                         crew: Option[Set[UUID]],
                         name: Option[Set[String]],
                         norms: Option[Set[String]]
                         ) {
  /** Extend a taking filter with given crew_id
   * @param crewId public_id of a Crew as UUID
   * @return with crew extended taking filter
   */
  def extend(crewId: UUID): TakingFilter = TakingFilter(this.publicId, Some(Set(crewId)), this.name, this.norms)
} 
/**Factory for [[TakingFilter]] instance. Can be handle as a json.*/
object TakingFilter {
  implicit val takingFilterFormat = Json.format[TakingFilter]
}

/** A class for handling takings query object
 * @param page
 * @param sort
 * @param filter
 */
case class TakingQueryBody(page: Option[Page], sort: Option[Sort], filter: Option[TakingFilter])
/** Factory for [[TakingQueryBody]] instance. Can be handle as a json.*/  
object TakingQueryBody {
  implicit val takingQueryBodyFormat = Json.format[TakingQueryBody]
}
