// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dls/Workspace/vca/pool/stream-backend/conf/routes
// @DATE:Mon Feb 10 12:49:23 CET 2020

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:7
  HomeController_5: controllers.HomeController,
  // @LINE:10
  Assets_2: controllers.Assets,
  // @LINE:13
  DropsController_4: controllers.DropsController,
  // @LINE:19
  TakingsController_1: controllers.TakingsController,
  // @LINE:26
  HouseholdController_3: controllers.HouseholdController,
  // @LINE:36
  DepositController_0: controllers.DepositController,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:7
    HomeController_5: controllers.HomeController,
    // @LINE:10
    Assets_2: controllers.Assets,
    // @LINE:13
    DropsController_4: controllers.DropsController,
    // @LINE:19
    TakingsController_1: controllers.TakingsController,
    // @LINE:26
    HouseholdController_3: controllers.HouseholdController,
    // @LINE:36
    DepositController_0: controllers.DepositController
  ) = this(errorHandler, HomeController_5, Assets_2, DropsController_4, TakingsController_1, HouseholdController_3, DepositController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HomeController_5, Assets_2, DropsController_4, TakingsController_1, HouseholdController_3, DepositController_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.HomeController.index"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/""" + "$" + """file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """authenticate/""" + "$" + """provider<[^/]+>""", """controllers.DropsController.authenticate(provider:String, route:Option[String], ajax:Option[Boolean])"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """authenticate/""" + "$" + """provider<[^/]+>""", """controllers.DropsController.authenticate(provider:String, route:Option[String], ajax:Option[Boolean])"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """identity""", """controllers.DropsController.frontendLogin"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """takings""", """controllers.TakingsController.get"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """takings/id/""" + "$" + """id<[^/]+>""", """controllers.TakingsController.getById(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """takings/count""", """controllers.TakingsController.count"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """takings/create""", """controllers.TakingsController.create"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """takings/update""", """controllers.TakingsController.update"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """household/count""", """controllers.HouseholdController.count"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """household""", """controllers.HouseholdController.read"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """household/create""", """controllers.HouseholdController.create"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """household/update""", """controllers.HouseholdController.update"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """household/state/action/""" + "$" + """uuid<[^/]+>/""" + "$" + """role<[^/]+>""", """controllers.HouseholdController.stateUpdate(uuid:String, role:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """deposits""", """controllers.DepositController.all(offset:Option[Int], size:Option[Int], name:Option[String], sortby:Option[String], sortdir:Option[String])"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """deposits/count""", """controllers.DepositController.count"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """deposits/create""", """controllers.DepositController.create"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """deposits/confirm""", """controllers.DepositController.confirm"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:7
  private[this] lazy val controllers_HomeController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_HomeController_index0_invoker = createInvoker(
    HomeController_5.index,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "index",
      Nil,
      "GET",
      this.prefix + """""",
      """ An example controller showing a sample home page""",
      Seq()
    )
  )

  // @LINE:10
  private[this] lazy val controllers_Assets_versioned1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned1_invoker = createInvoker(
    Assets_2.versioned(fakeValue[String], fakeValue[Asset]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      this.prefix + """assets/""" + "$" + """file<.+>""",
      """ Map static resources from the /public folder to the /assets URL path""",
      Seq()
    )
  )

  // @LINE:13
  private[this] lazy val controllers_DropsController_authenticate2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("authenticate/"), DynamicPart("provider", """[^/]+""",true)))
  )
  private[this] lazy val controllers_DropsController_authenticate2_invoker = createInvoker(
    DropsController_4.authenticate(fakeValue[String], fakeValue[Option[String]], fakeValue[Option[Boolean]]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DropsController",
      "authenticate",
      Seq(classOf[String], classOf[Option[String]], classOf[Option[Boolean]]),
      "GET",
      this.prefix + """authenticate/""" + "$" + """provider<[^/]+>""",
      """ OAuth Login with Drops""",
      Seq()
    )
  )

  // @LINE:14
  private[this] lazy val controllers_DropsController_authenticate3_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("authenticate/"), DynamicPart("provider", """[^/]+""",true)))
  )
  private[this] lazy val controllers_DropsController_authenticate3_invoker = createInvoker(
    DropsController_4.authenticate(fakeValue[String], fakeValue[Option[String]], fakeValue[Option[Boolean]]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DropsController",
      "authenticate",
      Seq(classOf[String], classOf[Option[String]], classOf[Option[Boolean]]),
      "POST",
      this.prefix + """authenticate/""" + "$" + """provider<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:16
  private[this] lazy val controllers_DropsController_frontendLogin4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("identity")))
  )
  private[this] lazy val controllers_DropsController_frontendLogin4_invoker = createInvoker(
    DropsController_4.frontendLogin,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DropsController",
      "frontendLogin",
      Nil,
      "GET",
      this.prefix + """identity""",
      """ WebApp route""",
      Seq()
    )
  )

  // @LINE:19
  private[this] lazy val controllers_TakingsController_get5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("takings")))
  )
  private[this] lazy val controllers_TakingsController_get5_invoker = createInvoker(
    TakingsController_1.get,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TakingsController",
      "get",
      Nil,
      "POST",
      this.prefix + """takings""",
      """ Takings""",
      Seq()
    )
  )

  // @LINE:20
  private[this] lazy val controllers_TakingsController_getById6_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("takings/id/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_TakingsController_getById6_invoker = createInvoker(
    TakingsController_1.getById(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TakingsController",
      "getById",
      Seq(classOf[String]),
      "GET",
      this.prefix + """takings/id/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:21
  private[this] lazy val controllers_TakingsController_count7_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("takings/count")))
  )
  private[this] lazy val controllers_TakingsController_count7_invoker = createInvoker(
    TakingsController_1.count,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TakingsController",
      "count",
      Nil,
      "POST",
      this.prefix + """takings/count""",
      """""",
      Seq()
    )
  )

  // @LINE:22
  private[this] lazy val controllers_TakingsController_create8_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("takings/create")))
  )
  private[this] lazy val controllers_TakingsController_create8_invoker = createInvoker(
    TakingsController_1.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TakingsController",
      "create",
      Nil,
      "POST",
      this.prefix + """takings/create""",
      """""",
      Seq()
    )
  )

  // @LINE:23
  private[this] lazy val controllers_TakingsController_update9_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("takings/update")))
  )
  private[this] lazy val controllers_TakingsController_update9_invoker = createInvoker(
    TakingsController_1.update,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TakingsController",
      "update",
      Nil,
      "POST",
      this.prefix + """takings/update""",
      """""",
      Seq()
    )
  )

  // @LINE:26
  private[this] lazy val controllers_HouseholdController_count10_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("household/count")))
  )
  private[this] lazy val controllers_HouseholdController_count10_invoker = createInvoker(
    HouseholdController_3.count,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HouseholdController",
      "count",
      Nil,
      "POST",
      this.prefix + """household/count""",
      """ Household""",
      Seq()
    )
  )

  // @LINE:27
  private[this] lazy val controllers_HouseholdController_read11_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("household")))
  )
  private[this] lazy val controllers_HouseholdController_read11_invoker = createInvoker(
    HouseholdController_3.read,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HouseholdController",
      "read",
      Nil,
      "POST",
      this.prefix + """household""",
      """""",
      Seq()
    )
  )

  // @LINE:28
  private[this] lazy val controllers_HouseholdController_create12_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("household/create")))
  )
  private[this] lazy val controllers_HouseholdController_create12_invoker = createInvoker(
    HouseholdController_3.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HouseholdController",
      "create",
      Nil,
      "POST",
      this.prefix + """household/create""",
      """""",
      Seq()
    )
  )

  // @LINE:29
  private[this] lazy val controllers_HouseholdController_update13_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("household/update")))
  )
  private[this] lazy val controllers_HouseholdController_update13_invoker = createInvoker(
    HouseholdController_3.update,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HouseholdController",
      "update",
      Nil,
      "POST",
      this.prefix + """household/update""",
      """""",
      Seq()
    )
  )

  // @LINE:30
  private[this] lazy val controllers_HouseholdController_stateUpdate14_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("household/state/action/"), DynamicPart("uuid", """[^/]+""",true), StaticPart("/"), DynamicPart("role", """[^/]+""",true)))
  )
  private[this] lazy val controllers_HouseholdController_stateUpdate14_invoker = createInvoker(
    HouseholdController_3.stateUpdate(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HouseholdController",
      "stateUpdate",
      Seq(classOf[String], classOf[String]),
      "POST",
      this.prefix + """household/state/action/""" + "$" + """uuid<[^/]+>/""" + "$" + """role<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:36
  private[this] lazy val controllers_DepositController_all15_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("deposits")))
  )
  private[this] lazy val controllers_DepositController_all15_invoker = createInvoker(
    DepositController_0.all(fakeValue[Option[Int]], fakeValue[Option[Int]], fakeValue[Option[String]], fakeValue[Option[String]], fakeValue[Option[String]]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DepositController",
      "all",
      Seq(classOf[Option[Int]], classOf[Option[Int]], classOf[Option[String]], classOf[Option[String]], classOf[Option[String]]),
      "GET",
      this.prefix + """deposits""",
      """""",
      Seq()
    )
  )

  // @LINE:37
  private[this] lazy val controllers_DepositController_count16_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("deposits/count")))
  )
  private[this] lazy val controllers_DepositController_count16_invoker = createInvoker(
    DepositController_0.count,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DepositController",
      "count",
      Nil,
      "POST",
      this.prefix + """deposits/count""",
      """""",
      Seq()
    )
  )

  // @LINE:38
  private[this] lazy val controllers_DepositController_create17_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("deposits/create")))
  )
  private[this] lazy val controllers_DepositController_create17_invoker = createInvoker(
    DepositController_0.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DepositController",
      "create",
      Nil,
      "POST",
      this.prefix + """deposits/create""",
      """""",
      Seq()
    )
  )

  // @LINE:39
  private[this] lazy val controllers_DepositController_confirm18_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("deposits/confirm")))
  )
  private[this] lazy val controllers_DepositController_confirm18_invoker = createInvoker(
    DepositController_0.confirm,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DepositController",
      "confirm",
      Nil,
      "POST",
      this.prefix + """deposits/confirm""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:7
    case controllers_HomeController_index0_route(params@_) =>
      call { 
        controllers_HomeController_index0_invoker.call(HomeController_5.index)
      }
  
    // @LINE:10
    case controllers_Assets_versioned1_route(params@_) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned1_invoker.call(Assets_2.versioned(path, file))
      }
  
    // @LINE:13
    case controllers_DropsController_authenticate2_route(params@_) =>
      call(params.fromPath[String]("provider", None), params.fromQuery[Option[String]]("route", None), params.fromQuery[Option[Boolean]]("ajax", None)) { (provider, route, ajax) =>
        controllers_DropsController_authenticate2_invoker.call(DropsController_4.authenticate(provider, route, ajax))
      }
  
    // @LINE:14
    case controllers_DropsController_authenticate3_route(params@_) =>
      call(params.fromPath[String]("provider", None), params.fromQuery[Option[String]]("route", None), params.fromQuery[Option[Boolean]]("ajax", None)) { (provider, route, ajax) =>
        controllers_DropsController_authenticate3_invoker.call(DropsController_4.authenticate(provider, route, ajax))
      }
  
    // @LINE:16
    case controllers_DropsController_frontendLogin4_route(params@_) =>
      call { 
        controllers_DropsController_frontendLogin4_invoker.call(DropsController_4.frontendLogin)
      }
  
    // @LINE:19
    case controllers_TakingsController_get5_route(params@_) =>
      call { 
        controllers_TakingsController_get5_invoker.call(TakingsController_1.get)
      }
  
    // @LINE:20
    case controllers_TakingsController_getById6_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_TakingsController_getById6_invoker.call(TakingsController_1.getById(id))
      }
  
    // @LINE:21
    case controllers_TakingsController_count7_route(params@_) =>
      call { 
        controllers_TakingsController_count7_invoker.call(TakingsController_1.count)
      }
  
    // @LINE:22
    case controllers_TakingsController_create8_route(params@_) =>
      call { 
        controllers_TakingsController_create8_invoker.call(TakingsController_1.create)
      }
  
    // @LINE:23
    case controllers_TakingsController_update9_route(params@_) =>
      call { 
        controllers_TakingsController_update9_invoker.call(TakingsController_1.update)
      }
  
    // @LINE:26
    case controllers_HouseholdController_count10_route(params@_) =>
      call { 
        controllers_HouseholdController_count10_invoker.call(HouseholdController_3.count)
      }
  
    // @LINE:27
    case controllers_HouseholdController_read11_route(params@_) =>
      call { 
        controllers_HouseholdController_read11_invoker.call(HouseholdController_3.read)
      }
  
    // @LINE:28
    case controllers_HouseholdController_create12_route(params@_) =>
      call { 
        controllers_HouseholdController_create12_invoker.call(HouseholdController_3.create)
      }
  
    // @LINE:29
    case controllers_HouseholdController_update13_route(params@_) =>
      call { 
        controllers_HouseholdController_update13_invoker.call(HouseholdController_3.update)
      }
  
    // @LINE:30
    case controllers_HouseholdController_stateUpdate14_route(params@_) =>
      call(params.fromPath[String]("uuid", None), params.fromPath[String]("role", None)) { (uuid, role) =>
        controllers_HouseholdController_stateUpdate14_invoker.call(HouseholdController_3.stateUpdate(uuid, role))
      }
  
    // @LINE:36
    case controllers_DepositController_all15_route(params@_) =>
      call(params.fromQuery[Option[Int]]("offset", None), params.fromQuery[Option[Int]]("size", None), params.fromQuery[Option[String]]("name", None), params.fromQuery[Option[String]]("sortby", None), params.fromQuery[Option[String]]("sortdir", None)) { (offset, size, name, sortby, sortdir) =>
        controllers_DepositController_all15_invoker.call(DepositController_0.all(offset, size, name, sortby, sortdir))
      }
  
    // @LINE:37
    case controllers_DepositController_count16_route(params@_) =>
      call { 
        controllers_DepositController_count16_invoker.call(DepositController_0.count)
      }
  
    // @LINE:38
    case controllers_DepositController_create17_route(params@_) =>
      call { 
        controllers_DepositController_create17_invoker.call(DepositController_0.create)
      }
  
    // @LINE:39
    case controllers_DepositController_confirm18_route(params@_) =>
      call { 
        controllers_DepositController_confirm18_invoker.call(DepositController_0.confirm)
      }
  }
}
