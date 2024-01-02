import Vue from "vue";
import Router from "vue-router";
import { store } from "../store";
import { routesList, routes } from "./routes";

Vue.use(Router)

let router = new Router({
  routes: routesList
});

// determine if user is authorized to access the route
router.beforeEach((to, from, next) => {
  // pre-construct login redirect
  let loginRedirect = {
    name: "Login",
    path: routes.LOGIN(),
    replace: true,
    query: {
      redirect: to.fullPath
    }
  };

  // determine if must be logged in
  if (!!to.meta && !!to.meta.loggedIn && !store.loggedIn) {
    console.log("Not logged in");
    next(loginRedirect);
  }

  // determine if have appropriate claims
  else if (!!to.meta && !!to.meta.userClaims) {
    // compare claims
    let claimFound = !to.meta.userClaims || to.meta.userClaims.length == 0;
    if (!claimFound && !!store.userClaims) {
      for (let claim of to.meta.userClaims) {
        if (store.userClaims.indexOf(claim) !== -1) {
          claimFound = true;
          break;
        }
      }
    }
    if (!claimFound) {
      console.log("Invalid claims");
      next(loginRedirect);
    }
    else {
      next();
    }
  }

  // complete navigation
  else {
    next();
  }
});

export default router;
