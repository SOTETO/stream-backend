package daos.reader

import java.util.UUID
import models.frontend.{Taking, Source, Amount, ExternalTransaction, TypeOfSource}
import slick.jdbc.GetResult
import models.frontend.ExternalTransaction

case class SourceReader(
                       id: Option[Long],
                       public_id: String,
                       taking_id: Long,
                       category: String,
                       amount: Double,
                       currency: String,
                       type_of_source: String,
                       type_location: Option[String],
                       type_contact_person: Option[String],
                       type_email: Option[String],
                       type_address: Option[String],
                       receipt: Option[Boolean],
                       norms: String
                       ) {
  def toSource: Source = {
    val externalTransaction: Option[ExternalTransaction] = type_location match {
      case Some(et) => Some(ExternalTransaction(et, type_contact_person.get, type_email.get, type_address.get, receipt.get))
      case None => None
    }
    Source(
    Some(UUID.fromString(public_id)), 
    category, 
    Amount(amount, currency), 
    TypeOfSource(type_of_source, externalTransaction),
    norms
  )}
}

object SourceReader extends ((Option[Long], String, Long, String, Double, String, String, Option[String], Option[String], Option[String], Option[String], Option[Boolean], String) => SourceReader) {
  
  def apply(source: Source, id: Long, taking_id: Long, public_id: String): SourceReader = {
    source.typeOfSource.external match {
       case  Some(external) => SourceReader(
         Some(id), 
         public_id, 
         taking_id, source.category, 
         source.amount.amount, 
         source.amount.currency, 
         source.typeOfSource.category,
         Some(external.location),
         Some(external.contactPerson),
         Some(external.email),
         Some(external.address),
         Some(external.receipt),
         source.norms
       )
       case None => SourceReader(
         None, 
         UUID.randomUUID().toString, 
         taking_id, source.category, 
         source.amount.amount, 
         source.amount.currency, 
         source.typeOfSource.category,
         None,
         None,
         None,
         None,
         None,
         source.norms
       )
    }
  }
  def apply(source: Source, taking_id: Long): SourceReader = {
    source.typeOfSource.external match {
       case  Some(external) => SourceReader(
         None,
         UUID.randomUUID().toString, 
         taking_id, source.category, 
         source.amount.amount, 
         source.amount.currency, 
         source.typeOfSource.category,
         Some(external.location),
         Some(external.contactPerson),
         Some(external.email),
         Some(external.address),
         Some(external.receipt),
         source.norms
       )
       case None => SourceReader(
         None, 
         UUID.randomUUID().toString, 
         taking_id, source.category, 
         source.amount.amount, 
         source.amount.currency, 
         source.typeOfSource.category,
         None,
         None,
         None,
         None,
         None,
         source.norms
       )
    }
  }


  def apply(tuple: (Option[Long], String, Long, String, Double, String, String, Option[String], Option[String], Option[String], Option[String], Option[Boolean], String)): SourceReader =
    SourceReader(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, tuple._9, tuple._10, tuple._11, tuple._12, tuple._13)

  implicit val getSourceReader = GetResult(r =>
    SourceReader(r.nextLongOption, r.nextString, r.nextLong, r.nextString, r.nextDouble, r.nextString, r.nextString, r.nextStringOption, r.nextStringOption, r.nextStringOption, r.nextStringOption, r.nextBooleanOption, r.nextString())
  )
}
