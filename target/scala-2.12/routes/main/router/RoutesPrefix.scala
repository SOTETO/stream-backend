// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dls/Workspace/vca/pool/stream-backend/conf/routes
// @DATE:Mon Feb 10 12:49:23 CET 2020


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
