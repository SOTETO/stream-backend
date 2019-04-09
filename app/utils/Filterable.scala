package utils

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


trait Filterable {
  val filterable: List[FilterableField]
}
