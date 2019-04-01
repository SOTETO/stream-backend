package controllers.restQuery.values

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class FilterField(entity: String, field: String) {
  def ~(entity: String, field: String): Boolean = this.entity == entity && this.field == field
}

case class FilterValue(field: FilterField, value: JsValue)

case class RESTFilter(fields: List[FilterValue]) {
  def getValue(entity: String, field: String): Option[Any] =
    fields.find(_.field ~ (entity, field)).map(_.value)

  def isDefined(entity: String, field: String) : Boolean =
    fields.exists(_.field ~ (entity, field))

  def ++(other: RESTFilter): RESTFilter =
    RESTFilter(this.fields ++ other.fields)

  def +++(others: Seq[RESTFilter]): RESTFilter =
    others.foldLeft[RESTFilter](this)((acc, current) => acc ++ current)
}

object FilterField {
  implicit val filterValueFormat = Json.format[FilterField]
}

object FilterValue {
  implicit val filterValueFormat = Json.format[FilterValue]
}

object RESTFilter {
  implicit val filterFormat = Json.format[RESTFilter]
}

trait Filterable {
  val filterFields: List[FilterField]

  val dependentFilter: List[Reads[RESTFilter]] = Nil

  val filterJson : Reads[RESTFilter] = Reads.apply(js => {
    def swapTypes[T](res : List[JsResult[T]]): JsResult[List[T]] = {
      // transform List[JsResult] into JsResult[List]
      val allErrors = res.collect {
        case JsError(errors) => errors
      }.flatten

      if (allErrors.nonEmpty) {
        JsError(allErrors)
      } else {
        JsSuccess(res.collect {
          case JsSuccess(a, _) => a
        })
      }
    }

    val dependencies = swapTypes[RESTFilter](dependentFilter.map(df =>
      js.validateOpt[RESTFilter](df).filter(_.isDefined).map(_.get)
    ))
    val mappedFilter = filterFields
      .filter(field => (js \ field.entity \ field.field).isDefined)
      .map(field => FilterValue( field, (js \ field.entity \ field.field).get))

    dependencies.map(dl => RESTFilter( mappedFilter ) +++ dl)
  })
}
