package utils

/**
  * Maps a {{{FilterableField}}} to some kind of "consequence", for example a sorting operation on the set of business
  * objects. It has to be implemented for all declarative defined {{{FilterableFields}}} on a companion object of a class
  * that implements a business object. This has to be done in a DAO class.
  *
  * @author Johann Sell
  * @param field describes the field, the "consequences" are defined for
  * @param toSortOperation a possible "consequence" - a function that sorts a set of business objects based on the given field
  * @tparam DATATYPE the class of the business object
  * @tparam VALUE class of potentially additional parameter used to calculate the "consequences"
  */
case class FilteringOperation[DATATYPE, VALUE](
                                              field: FilterableField,
                                              toSortOperation : Function1[VALUE, Function2[DATATYPE, DATATYPE, Boolean]]
                                            )

/**
  * Implements a trait to apply the {{FilteringOperation}} onto a class or DAO.
  *
  * @author Johann Sell
  * @tparam DATATYPE the class of the business object
  * @tparam VALUE class of potentially additional parameter used to calculate the "consequences"
  */
trait Filter[DATATYPE, VALUE] {
  val operations : List[FilteringOperation[DATATYPE, VALUE]]
}