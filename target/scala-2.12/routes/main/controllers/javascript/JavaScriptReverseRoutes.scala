// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dls/Workspace/VcA/Pool2/stream-backend/conf/routes
// @DATE:Wed Jan 29 16:51:59 CET 2020

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:7
package controllers.javascript {

  // @LINE:10
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def versioned: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.versioned",
      """
        function(file1) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[play.api.mvc.PathBindable[Asset]].javascriptUnbind + """)("file", file1)})
        }
      """
    )
  
  }

  // @LINE:13
  class ReverseDropsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:13
    def authenticate: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DropsController.authenticate",
      """
        function(provider0,route1,ajax2) {
        
          if (true) {
            return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "authenticate/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("provider", provider0)) + _qS([(""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("route", route1), (""" + implicitly[play.api.mvc.QueryStringBindable[Option[Boolean]]].javascriptUnbind + """)("ajax", ajax2)])})
          }
        
        }
      """
    )
  
    // @LINE:16
    def frontendLogin: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DropsController.frontendLogin",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "identity"})
        }
      """
    )
  
  }

  // @LINE:7
  class ReverseHomeController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def index: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.index",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + """"})
        }
      """
    )
  
  }

  // @LINE:19
  class ReverseTakingsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:22
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TakingsController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "takings/create"})
        }
      """
    )
  
    // @LINE:21
    def count: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TakingsController.count",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "takings/count"})
        }
      """
    )
  
    // @LINE:19
    def get: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TakingsController.get",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "takings"})
        }
      """
    )
  
    // @LINE:20
    def getById: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TakingsController.getById",
      """
        function(id0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "takings/id/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:23
    def update: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TakingsController.update",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "takings/update"})
        }
      """
    )
  
  }

  // @LINE:36
  class ReverseDepositController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def all: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DepositController.all",
      """
        function(offset0,size1,name2,sortby3,sortdir4) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "deposits" + _qS([(""" + implicitly[play.api.mvc.QueryStringBindable[Option[Int]]].javascriptUnbind + """)("offset", offset0), (""" + implicitly[play.api.mvc.QueryStringBindable[Option[Int]]].javascriptUnbind + """)("size", size1), (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("name", name2), (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("sortby", sortby3), (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("sortdir", sortdir4)])})
        }
      """
    )
  
    // @LINE:38
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DepositController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "deposits/create"})
        }
      """
    )
  
    // @LINE:39
    def confirm: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DepositController.confirm",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "deposits/confirm"})
        }
      """
    )
  
    // @LINE:37
    def count: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DepositController.count",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "deposits/count"})
        }
      """
    )
  
  }

  // @LINE:26
  class ReverseHouseholdController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:28
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HouseholdController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "household/create"})
        }
      """
    )
  
    // @LINE:27
    def read: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HouseholdController.read",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "household"})
        }
      """
    )
  
    // @LINE:26
    def count: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HouseholdController.count",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "household/count"})
        }
      """
    )
  
    // @LINE:30
    def stateUpdate: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HouseholdController.stateUpdate",
      """
        function(uuid0,role1) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "household/state/action/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("uuid", uuid0)) + "/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("role", role1))})
        }
      """
    )
  
    // @LINE:29
    def update: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HouseholdController.update",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "household/update"})
        }
      """
    )
  
  }


}
