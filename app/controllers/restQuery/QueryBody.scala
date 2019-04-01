//package controllers.restQuery
//
////import daos.{MariadbUserDao, UserDao}
////import models.dbviews._
////import models.User
//import play.api.i18n.Messages
//import play.api.libs.json.{Format, Json}
//
//import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
//
//case class QueryBody[T](
//                 query: Option[String],
//                 values: Option[T],
//                 sort : Option[Sort],
//                 offset: Option[Long],
//                 limit: Option[Long]
//                    )
//object QueryBody {
//  implicit val queryBodyFormat: Format[T] = Json.format[QueryBody[T]]
//
//  case class NoValuesGiven(msg: String) extends Exception(msg)
//  case class NoQueryGiven(msg: String) extends Exception(msg)
//
//  private def asRequest[T](body: QueryBody[T], view: String, ignorePagination : Boolean = false)(implicit messages: Messages) : Either[Exception, Converter] = {
//    val page = if(!ignorePagination) body.limit.map(l => Page(l, body.offset)) else None
//
//    body.query.map(QueryLexer( _ ) match {
//      case Left(error) => Left(error)
//      case Right(tokens) => body.values.map(QueryParser(tokens, _ ) match {
//        case Left(error) => Left(error)
//        case Right(ast) => Right(ast)
//      }).getOrElse(Left(NoValuesGiven(Messages("rest.api.noValuesGiven"))))
//    }).getOrElse(Left(NoQueryGiven(Messages("rest.api.noQueryGiven")))) match { // got either an error or an AST
//      case Left(error: NoQueryGiven) => {
//        Right(Converter(view, None, page, body.sort))
//      }
//      case Left(error : Exception) => Left(error)
//      case Right(ast) => {
//        Right(Converter(view, Some(ast), page, body.sort))
//      }
//    }
//  }
//
//  def asCount[T](body: QueryBody[T], view: String)(implicit messages: Messages) : Either[Exception, Converter] =
//    asRequest[T](body, view, true)
//}
//
