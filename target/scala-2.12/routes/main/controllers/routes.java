// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dls/Workspace/vca/pool/stream-backend/conf/routes
// @DATE:Mon Feb 10 12:49:23 CET 2020

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseAssets Assets = new controllers.ReverseAssets(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseDropsController DropsController = new controllers.ReverseDropsController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseHomeController HomeController = new controllers.ReverseHomeController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseTakingsController TakingsController = new controllers.ReverseTakingsController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseDepositController DepositController = new controllers.ReverseDepositController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseHouseholdController HouseholdController = new controllers.ReverseHouseholdController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseAssets Assets = new controllers.javascript.ReverseAssets(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseDropsController DropsController = new controllers.javascript.ReverseDropsController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseHomeController HomeController = new controllers.javascript.ReverseHomeController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseTakingsController TakingsController = new controllers.javascript.ReverseTakingsController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseDepositController DepositController = new controllers.javascript.ReverseDepositController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseHouseholdController HouseholdController = new controllers.javascript.ReverseHouseholdController(RoutesPrefix.byNamePrefix());
  }

}
