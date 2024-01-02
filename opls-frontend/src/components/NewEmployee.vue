<template>
  <div id="newservice">
    <h1>Create a new employee</h1>
    

      <form @submit.prevent="sendRequest">
      <label for="username">Username</label>
      <br>
      <input type="text" id="username" name="username" v-model="username">
      <br>
      <br>
      <label for="firstName">First name</label>
      <br>
      <input type="text" id="firstName" name="firstName" v-model="firstName">
      <br>
      <br>
      <label for="lastName">Last name</label>
      <br>
      <input type="text" id="lastName" name="lastName" v-model="lastName">
      <br>
      <br>
      <label for="password">Password</label>
      <br>
      <input type="text" id="password" name="password" v-model="password">
      <br>
      <br>
      <label for="securityQuestion">Security question</label>
      <br>
      <input type="text" id="securityQuestion" name="securityQuestion" v-model="securityQuestion">
      <br>
      <br>
      <label for="securityAnswer">Security question answer</label>
      <br>
      <input type="text" id="securityAnswer" name="securityAnswer" v-model="securityAnswer">
      <br>
      <br>
      <label for="jobTitle">Job title</label>
      <br>
      <input type="text" id="jobTitle" name="jobTitle" v-model="jobTitle">
      <br>
      <br>
      <label for="salary">Salary</label>
      <br>
      <input type="text" id="salary" name="salary" v-model="salary">
      <br>
      <br>
      <input type="submit" value="Create Employee">
      <br>
      <br>
    </form>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";
export default {
  name: 'newEmployee',
  data () {
    return {
      username: '',
      firstName: '',
      lastName: '',
      password: '',
      securityQuestion: '',
      securityAnswer: '',
      userAccountUUID: '',
      jobTitle: '',
      salary: 0,
      license: 'default'
    }
  },
  methods:{
      async sendRequest(){
        try{
          var x = await authenticatedRequest("POST", "/account",new Account(this.username, this.firstName, this.lastName, this.password, this.securityQuestion, this.securityAnswer));
          this.userAccountUUID = x.uuid;
          authenticatedRequest("POST", "/customer", new Customer(this.userAccountUUID, 10, this.license));
          var response = await authenticatedRequest("POST", "/employee",new Employee(this.userAccountUUID, this.jobTitle, this.salary));
          alert("Employee created successfully")
        }catch(error) {
          alert("An error has occured while creating an employee: " + error.data.error);
        }
        }
        

  }
}
function Account(username, firstName, lastName, password, securityQuestion, securityAnswer){
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.securityQuestion = securityQuestion;
    this.securityAnswer = securityAnswer;
  }

function Employee(userAccountUUID, jobTitle, salary){
    this.userAccountUUID = userAccountUUID;
    this.jobTitle = jobTitle;
    this.salary = salary;
  }

  function Customer(userAccountUUID, billingAccountId, savedLicensePlate){
    this.userAccountUUID = userAccountUUID;
    this.billingAccountId = billingAccountId;
    this.savedLicensePlate = savedLicensePlate;
  }
</script>

