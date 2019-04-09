package utils

case class FilteringOperation[DATATYPE, VALUE](
                                              field: FilterableField,
                                              toSortOperation : Function1[VALUE, Function2[DATATYPE, DATATYPE, Boolean]]
                                            )

trait Filter[DATATYPE, VALUE] {
  val operations : List[FilteringOperation[DATATYPE, VALUE]]
}