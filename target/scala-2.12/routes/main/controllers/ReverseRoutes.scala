// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dls/Workspace/VcA/Pool2/stream-backend/conf/routes
// @DATE:Wed Jan 29 16:51:59 CET 2020

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:7
package controllers {

  // @LINE:10
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def versioned(file:Asset): Call = {
      implicit lazy val _rrc = new play.core.routing.ReverseRouteContext(Map(("path", "/public"))); _rrc
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[play.api.mvc.PathBindable[Asset]].unbind("file", file))
    }
  
  }

  // @LINE:13
  class ReverseDropsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:13
    def authenticate(provider:String, route:Option[String], ajax:Option[Boolean]): Call = {
    
      (provider: @unchecked, route: @unchecked, ajax: @unchecked) match {
      
        // @LINE:13
        case (provider, route, ajax)  =>
          
          Call("GET", _prefix + { _defaultPrefix } + "authenticate/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("provider", provider)) + play.core.routing.queryString(List(Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("route", route)), Some(implicitly[play.api.mvc.QueryStringBindable[Option[Boolean]]].unbind("ajax", ajax)))))
      
      }
    
    }
  
    // @LINE:16
    def frontendLogin(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "identity")
    }
  
  }

  // @LINE:7
  class ReverseHomeController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def index(): Call = {
      
      Call("GET", _prefix)
    }
  
  }

  // @LINE:19
  class ReverseTakingsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:22
    def create(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "takings/create")
    }
  
    // @LINE:21
    def count(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "takings/count")
    }
  
    // @LINE:19
    def get(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "takings")
    }
  
    // @LINE:20
    def getById(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "takings/id/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:23
    def update(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "takings/update")
    }
  
  }

  // @LINE:36
  class ReverseDepositController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def all(offset:Option[Int], size:Option[Int], name:Option[String], sortby:Option[String], sortdir:Option[String]): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "deposits" + play.core.routing.queryString(List(Some(implicitly[play.api.mvc.QueryStringBindable[Option[Int]]].unbind("offset", offset)), Some(implicitly[play.api.mvc.QueryStringBindable[Option[Int]]].unbind("size", size)), Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("name", name)), Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("sortby", sortby)), Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("sortdir", sortdir)))))
    }
  
    // @LINE:38
    def create(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "deposits/create")
    }
  
    // @LINE:39
    def confirm(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "deposits/confirm")
    }
  
    // @LINE:37
    def count(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "deposits/count")
    }
  
  }

  // @LINE:26
  class ReverseHouseholdController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:28
    def create(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "household/create")
    }
  
    // @LINE:27
    def read(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "household")
    }
  
    // @LINE:26
    def count(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "household/count")
    }
  
    // @LINE:30
    def stateUpdate(uuid:String, role:String): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "household/state/action/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("uuid", uuid)) + "/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("role", role)))
    }
  
    // @LINE:29
    def update(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "household/update")
    }
  
  }


}
