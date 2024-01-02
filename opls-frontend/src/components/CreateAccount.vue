<template>
  <div class="hello">
    <h1>Create Account</h1>
    <p></p>
    <h2>Your Profile</h2>

    <form>
      <label>Username:</label>
      <input type="text" v-model.trim="newUsername" /> <br />
      <label>First name:</label>
      <input type="text" v-model.trim="newFirstName" /> <br />
      <label>Last name:</label>
      <input type="text" v-model.trim="newLastName" /> <br />
      <label>Password:</label>
      <input type="password" v-model.trim="newPassword" /> <br />
      <label>Security question:</label>
      <input type="text" v-model.trim="newSecurityQuestion" /> <br />
      <label>Security answer:</label>
      <input type="password" v-model.trim="newSecurityAnswer" /> <br />
      <button v-bind:disabled="!newUsername || !newFirstName || !newLastName || 
        !newPassword || !newSecurityQuestion || !newSecurityAnswer "
                @click="createAccount">Create Account</button>
      <br />
      <p style="color: red" v-if="error">{{ error }}</p>
      <p></p>
    </form>
  </div>
</template>

<script>
  function UserAccountRequestDto(username, firstName, lastName, password, securityQuestion, securityQuestionAnswer) {
    return {
      username: username,
      firstName: firstName,
      lastName: lastName,
      password: password,
      securityQuestion: securityQuestion,
      securityAnswer: securityQuestionAnswer,
    }
  }

  import { routes } from "../router/routes"
  import { authenticatedRequest, login } from "./apiclient";

  export default {
    name: 'createAccount',
    data() {
      return {
        newUsername: '',
        newFirstName:'',
        newLastName: '',
        newPassword: '',
        newSecurityQuestion: '',
        newSecurityAnswer: '',
        error: '',
      }
    },
    methods: {
      createAccount() {
        authenticatedRequest("POST", "/account",
          UserAccountRequestDto(this.newUsername, this.newFirstName, this.newLastName, this.newPassword, this.newSecurityQuestion, this.newSecurityAnswer))
          .then((data) => {
            // redirect to login page
            this.$router.push({
              path: routes.LOGIN(),
              query: {
                redirect: routes.ACCOUNT_INFORMATION(),
                reason: "Please login to create your customer profile."
              }
            });
          })
          .catch((error) => {
            this.error = error.data.error_description;
          });
      },
    }
  }
</script>
