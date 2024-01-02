import { reactive } from "vue";
import { detectAuthStore } from "../components/apiclient";

export const store = reactive({
  loggedIn: false,
  userClaims: [],
  login() {
    this.loggedIn = true;
  },
  logout() {
    this.loggedIn = false;
    this.userClaims = [];
  },
  setClaims(claims) {
    this.userClaims = claims || [];
  },
  /** Return whether the current user has any of the specified claims. */
  hasClaim(...claims) {
    for (var claim of claims) {
      if (this.userClaims.indexOf(claim) !== -1) {
        return true;
      }
    }
    return false;
  },
});

export const initAuthStore = async function() {
  let authStore = await detectAuthStore();
  if (!!authStore && authStore.loggedIn) {
    store.login();
    store.setClaims(authStore.userClaims);
  }
}

export const initStore = async function() {
  await initAuthStore();
};
