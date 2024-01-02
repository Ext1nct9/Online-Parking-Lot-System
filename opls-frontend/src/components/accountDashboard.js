  function UserAccountResponseDto(username, firstName, lastName, securityQuestion, uuid, claims){
    this.username = username
    this.firstName = firstName
    this.lastName = lastName
    this.securityQuestion = securityQuestion
    this.uuid = uuid
  } 

  function UpdateUserAccountRequestDto (username, firstName, lastName) {
    this.username = username
    this.firstName = firstName
    this.lastName = lastName
  }

  function ResetPasswordRequestDto (password, securityAnswer) {
    this.password = password
    this.securityAnswer = securityAnswer
  }

  function ResetSecurityQuestionAnswerRequestDto (password, securityQuestion, securityAnswer){
    this.password = password
    this.securityQuestion = securityQuestion
    this.securityAnswer = securityAnswer
  }

  function CustomerDto (billingAccountId, savedLicensePlate){
    this.billingAccountId = billingAccountId
    this.savedLicensePlate = savedLicensePlate
  }

  import { authenticatedRequest } from "./apiclient";
  import { store } from "../store"
import { claims } from "../router/routes"

  export default {
    name: "Account",
    data() {
      return {
        currentUser: '',
        newUsername: '',
        newFirstName: '',
        newLastName: '',
        oldPass: '',
        newPass: '',
        newQuestion: '',
        oldA: '',
        newAnswer: '',
        currentCustomer: undefined,
        newBillingAccountId: 0,
        newSavedLicensePlate: ''
      }
    },
    mounted() {
      this.getUserAccount()
      this.getCustomer()
    },
    methods: {
      getUserAccount() {
        authenticatedRequest("GET", "/account", null).then((data) => {
          this.currentUser = new UserAccountResponseDto(
            data.username, data.firstname, data.lastname, data.securityQuestion, data.uuid);
        })
      },
      getCustomer() {
        if (store.hasClaim(claims.customer)) {
          authenticatedRequest("GET", "/customer", null).then((data) => {
            this.currentCustomer = new CustomerDto(
              data.billingAccountId, data.savedLicensePlate);
          });
        }
      },
      updateAccount: function (username, firstName, lastName) {
        authenticatedRequest("PUT", "/account",
          new UpdateUserAccountRequestDto(username, firstName, lastName))
          .then((data) => {
          this.currentUser = new UserAccountResponseDto(
            data.username, data.firstname, data.lastname, data.securityQuestion, data.uuid);
          })
          .catch((error) => {
            alert(error.data.error);
          })
        this.newUsername = ''
        this.newFirstName = ''
        this.newLastName = ''
      },
      updateCustomer: function (billingAccountId,savedLicensePLate) {
        let reqMethod = store.hasClaim(claims.customer) ? "PATCH" : "POST";
        authenticatedRequest(reqMethod, "/customer",
          new CustomerDto(billingAccountId, savedLicensePLate))
          .then((data) => {
          this.currentCustomer = new CustomerDto(
            data.billingAccountId, data.savedLicensePlate);
          })
          .catch((error) => {
            alert(error.data.error);
          });
        
        this.newBillingAccountId = 0
        this.newSavedLicensePlate = ''
      },
      updatePassword: function (password, securityQuestion, securityAnswer) {
        authenticatedRequest("PUT", "/account/resetPassword",
          new ResetPasswordRequestDto(password, securityAnswer))
          .then((data) => {
            this.currentUser = new UserAccountResponseDto(
              data.username, data.firstname, data.lastname, data.securityQuestion, data.uuid);
          })
          .catch((error) => {
            alert(error.data.error);
          })
        this.newPass = ''
        this.oldA = ''
      },

      updateSecurity: function (securityQuestion, securityAnswer, password) {
        authenticatedRequest("PUT", "/account/security",
          new ResetSecurityQuestionAnswerRequestDto(password,securityQuestion, securityAnswer))
          .then((data) => {
            this.currentUser = new UserAccountResponseDto(
              data.username, data.firstname, data.lastname, data.securityQuestion, data.uuid);
          })
          .catch((error) => {
            alert(error.data.error);
          })
        this.oldPass = ''
        this.newQuestion = 
        this.newAnswer = ''
      },
      

    }
  }
