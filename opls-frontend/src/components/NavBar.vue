<template>
  <div id="navbar">
    <div v-for="menuItem in menuItems">
      <router-link v-if="menuItem.link" :to="`${menuItem.link}`">{{ menuItem.displayName }}</router-link>
      <a v-if="menuItem.click" @click="menuItem.click">{{ menuItem.displayName }}</a>
    </div>
    <div v-if="userClaims.length > 0">
      <select v-model="view" @change="changeView">
        <option v-for="claim in userClaims" v-bind:value="claim">{{ claim }}</option>
      </select>
    </div>
  </div>
</template>

<script>
import { logout } from "./apiclient";
import { store } from "./../store";
import { routes, claims } from "./../router/routes";

export default {
  name: 'navbar',
  props: {
    loggedIn: Boolean
  },
  watch: {
    loggedIn: {
      immediate: true,
      handler(newValue, oldValue) {
        // update bar view when logged in prop changes
        this.changeView();
      }
    }
  },
  data() {
    return {
      view: store.userClaims && store.userClaims.length > 0
        ? claims.customer
        : store.userClaims,
      userClaims: store.userClaims,
      menuItems: this.changeView(),
    };
  },
  methods: {
    changeView() {
      let list = [];
      let view = this.view || claims.customer;
      let dashboardLink = undefined;

      // customize based on requested view
      switch (view) {
        case claims.customer:
          list = [{
            displayName: "Parking spots",
            link: routes.PARKING_SPOT_LIST(),
          }, {
            displayName: "New spot booking",
            link: routes.NEW_SPOT_BOOKING(),
          }, {
            displayName: "New service booking",
            link: routes.NEW_SERVICE_BOOKING(),
          }];
          dashboardLink = routes.CUSTOMER_DASHBOARD();
          break;
        case claims.employee:
          list = [{
            displayName: "Parking spots",
            link: routes.PARKING_SPOT_LIST(),
          }, {
            displayName: "Parking spot bookings",
            link: routes.SPOT_BOOKING_LIST(),
          }, {
            displayName: "Service bookings",
            link: routes.SERVICE_BOOKING_LIST(),
          }];
          dashboardLink = routes.EMPLOYEE_DASHBOARD();
          break;
        case claims.admin:
          list = [{
            displayName: "Parking spots",
            link: routes.PARKING_SPOT_LIST(),
          }, {
            displayName: "Employees",
            link: routes.EMPLOYEE_LIST(),
          }, {
            displayName: "Services",
            link: routes.SERVICE_LIST(),
          }];
          dashboardLink = routes.ADMIN_DASHBOARD();
          break;
      };

      if (store.loggedIn) {
        // add routes for all logged in users
        list = [{
          displayName: "Dashboard",
          link: dashboardLink
        }, ...list];
        list.push({
          displayName: "Account",
          link: routes.ACCOUNT_INFORMATION(),
        });
        list.push({
          displayName: "Logout",
          click: this.logoutUser,
        });
      }
      else {
        // add routes for all unregistered users
        list.push({
          displayName: "Login",
          link: routes.LOGIN(),
        }, {
          displayName: "Create account",
          link: routes.CREATE_ACCOUNT(),
        });
      }

      // display index link for all users
      list = [{
          displayName: "Home",
          link: routes.HELLO(),
        }, ...list];

      // update data
      this.menuItems = list;
      this.userClaims = store.userClaims;
      return list;
    },
    loginUser() {
      // determine optional redirect
      let query = {};
      if (this.$route.path !== routes.LOGIN()) {
        query = {
          redirect: this.$route.path
        };
      }

      // redirect to login page
      this.$router.push({
        path: routes.LOGIN(),
        query: query
      });
    },
    logoutUser() {
      // logout user in store
      logout();
      store.logout();

      // update nav bar through parent prop
      this.view = claims.customer;
      this.$emit("logout");

      // redirect if necessary
      if (this.$route.path !== routes.HELLO()) {
        this.$router.push({
          path: routes.HELLO()
        });
      }
    }
  }
}
</script>

<style>
#navbar {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: baseline;
  gap: 0px 30px;
  padding: 30px;
}

#navbar a {
  color: #007bff;
  text-decoration: none;
  cursor: pointer;
}
</style>
