// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dls/Workspace/VcA/Pool2/stream-backend/conf/routes
// @DATE:Wed Jan 29 16:51:59 CET 2020


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
