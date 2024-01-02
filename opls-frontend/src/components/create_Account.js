function UserAccountRequestDto(username, firstName, lastName, password, securityQuestion, securityQuestionAnswer){
    this.username = username
    this.firstName = firstName
    this.lastName = lastName
    this.password = password
    this.securityQuestion = securityQuestion
    this.securityAnswer = securityQuestionAnswer 
}

import { routes } from "../router/routes"
import { authenticatedRequest, login } from "./apiclient";

export default {
  data() {
    return {
      newUsername: '',
      newFirstName:'',
      newLastName: '',
      newPassword: '',
      newSecurityQuestion: '',
      newSecurityAnswer: '',
    }
  },
  methods: {
    createAccount() {
      authenticatedRequest("POST", "/account",
        UserAccountRequestDto(this.newUsername, this.newFirstName, this.newLastName, this.newPassword, this.newSecurityQuestion, this.newSecurityAnswer))
        .then((data) => {
          // create customer profile
          login(username, password)
            .then((data) => {
              this.$router.push({
                path: routes.ACCOUNT_INFORMATION()
              });
            })
            .catch((err) => {
              alert(err);
            });
        })
        .catch((error) => {
          alert(error);
        });
    },
  }
}
  