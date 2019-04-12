package utils

/**
  * Describes a filterable attribute of a business object by a string. There is no "real" connection between such a field
  * and an attribute of a class, but it can be used as a declarative description of strings that can be interpretated as
  * filterable attribute. It should be used by {{{trait Filterable}}}:
  *
  * {{{
  *   object User extends Filterable {
  *     override val filterable = List(FilterableField("user.name")) // example
  *   }
  * }}}
  * @author Johann Sell
  * @param path the string identifier that is used to mark an attribute
  * @param separator if the path contains a separator sign, it can be used to operate on the identifier
  */
case class FilterableField(path: String, separator: Option[String] = Some(".")) {
  override def equals(o: scala.Any): Boolean = o match {
    case other: FilterableField => this.path == other.path
//      this.separator match {
//      case Some(sep1) => this.path.split(sep1).zipAll(other.separator match {
//        case Some(sep2) => other.path.split(sep2)
//        case None => List(other)
//      }, "Not fitting", "Not fitting").forall(pair => {
//        println(pair)
//        pair._1 == pair._2
//      })
//      case None => this.path == other.path
//    }
    case _ => false
  }
}

/**
  * Implements a trait to apply the {{{FilterableField}}} onto companion objects of classes for business object.
  * {{{
  *  object User extends Filterable {
  *    override val filterable = List(FilterableField("user.name")) // example
  *  }
  * }}}
  * @author Johann Sell
  */
trait Filterable {
  val filterable: List[FilterableField]
}
