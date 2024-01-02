<template>
  <div class="hello">
    <h1>Login</h1>
    <form>
      <h3>Authentication</h3>
      <label>Username:</label>
      <input type="text" id="username" />
      <br>
      <label>Password:</label>
      <input type="password" id="password" />
      <button @click="loginUser">Login</button>
      <p style="color:red">{{ reason }}</p>
    </form>
  </div>
</template>

<script>
import { login } from "./apiclient";
import { initAuthStore } from "../store";
import { routes } from "../router/routes";


export default {
  name: 'login',
  data () {
    let reason = this.$route.query
      ? this.$route.query.reason
      : "";
    return {
      msg: 'Login',
      reason: reason
    }
  },
  methods: {
    loginUser() {
      const username = document.querySelector("#username").value;
      const password = document.querySelector("#password").value;
      login(username, password)
        .then((data) => {
          initAuthStore()
            .then(() => {

              this.$parent.$data.loggedIn = true;
              if (this.$route.query.redirect && this.$route.query.redirect != routes.CREATE_ACCOUNT()) {
                this.$router.push(this.$route.query.redirect);
              }
              else {
                this.$router.push(routes.HELLO());
              }
            });
        })
        .catch((err) => {
          this.reason = err;
        });
    },
  }
}
</script>
