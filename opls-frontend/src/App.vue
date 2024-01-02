<template>
  <div id="app">
    <NavBar v-bind:logged-in="loggedIn" @logout="logout" />
    <router-view></router-view>
  </div>
</template>

<script>
import NavBar from "./components/NavBar.vue";
import { store } from "./store";

export default {
  name: "app",
  data() {
    return {
      loggedIn: store.loggedIn
    };
  },
  methods: {
    login() {
      this.loggedIn = true;
    },
    logout() {
      this.loggedIn = false;
    },
  },
  components: { NavBar }
}
</script>

<style>
#app {
  font-family: 'Calibri', Calibri, Calibri, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
