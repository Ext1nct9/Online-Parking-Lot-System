<template>
  <div class="hello">
    <h1>Employee List</h1>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); grid-gap: 10px;">
      <div v-for="e in employees" style = "border: 1px solid #000;">
        <router-link v-if="employeesNames[e.userAccountUUID] != null" :to="`/employee/${e.uuid}`">{{employeesNames[e.userAccountUUID]}}</router-link>
        <p>Uuid: {{e.uuid}}</p>
        <p>Job Title: {{e.jobTitle}}</p>
        <p>Salary: {{e.salary}} $</p>
        <button @click="deleteEmployee(e.uuid)">Delete Employee</button>
      </div>
    </div>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";


export default {
  name: 'employeeList',
  data () {
    return {
      employees: [],
      employeesNames: {}
    }
  },
  mounted(){
    this.getEmployees();
  },
  methods:{
    async getEmployees(){
      try {
        var response = await authenticatedRequest("POST", `/employee/search`);
        if (Array.isArray(response)){
          this.employees = response;
        }
        else{
          this.employees = [response];
        }
        for (var i = 0; i < this.employees.length; i++){ // fetching names
          this.$set(this.employeesNames, this.employees[i].userAccountUUID, await this.getName(this.employees[i].userAccountUUID));
        }
      } catch (error) {
        alert("An unexpected error has occured while fetching employees: " + error.data.error);
      }
    },
    async getName(uuid){
      try {
        var response = await authenticatedRequest("GET", `/account/${uuid}`);
        return response.firstname + " " + response.lastname;
      } catch (error) {
        alert("An unexpected error has occured while fetching employee name: " + error.data.error);
      }
    },
    async deleteEmployee(uuid){
      try {
        var response = await authenticatedRequest("DELETE", `/employee/${uuid}`);
        this.getEmployees();
        alert(`Employee deleted successfully`);
      } catch (error) {
        alert("An error has occured while deleting employee: " + error.data.error);
      }
    },
  },
}
</script>
