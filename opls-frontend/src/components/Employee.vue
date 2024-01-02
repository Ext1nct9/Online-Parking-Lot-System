<template>
  <div class="employeemodification">
    <h1>Modify an Employee</h1>
    <p>{{ employee }}</p>
    First name: {{ firstname }} <br>
    Last name: {{ lastname }} <br>
    Job title: {{ jobtitle }} <br>
    Salary: {{ salary }} <br>
    <label for="jobTitle">Job title</label>
    <br>
    <input type="text" id="jobtitle" name="jobtitle" v-model="newJobTitle">
    <br>
    <br>
    <label for="salary">Salary</label>
    <br>
    <input type="text" id="salary" name="salary" v-model="newSalary">
    <br>
    <br>
    <button @click="modifyEmployee()">Update</button>
    <p></p>
    <h2>Your Schedule</h2>
    <table>
      <tr>
        <th>Day</th>
        <th>Start Time</th>
        <th>End Time</th>
      </tr>
      <tr v-for="schedule in schedules" :key="schedule.day">
        <td>{{ schedule.day }}</td>
        <td>{{ schedule.startTime }}</td>
        <td>{{ schedule.endTime }}</td>
      </tr>
    </table>
    <p></p>
      <h2>Create or Update Schedule</h2>
    
    Day: <select v-model="newDay">
         <option value="">Please select a day</option>
         <option value="MONDAY">Monday</option>
         <option value="TUESDAY">Tuesday</option>
         <option value="WEDNESDAY">Wednesday</option>
         <option value="THURSDAY">Thursday</option>
         <option value="FRIDAY">Friday</option>
         <option value="SATURDAY">Saturday</option>
         <option value="SUNDAY">Sunday</option>
    </select>
    <br>
    Start time: <input type="text" v-model.trim="newStartTime"> <br>
    End time: <input type="text" v-model.trim="newEndTime"> <br>
    <button v-bind:disabled="!newDay || !newStartTime || !newEndTime"
            @click="createSchedule(newDay, newStartTime, newEndTime)">Create Schedule</button>
    <button v-bind:disabled="!newDay || !newStartTime || !newEndTime"
            @click="updateSchedule(newDay, newStartTime, newEndTime)">Update Schedule</button>
    <button v-bind:disabled="!newDay"
            @click="deleteSchedule(newDay)">Delete Schedule</button>
    <p></p>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";

export default {
  name: 'employee',
  data () {
    return {
      employee: `${this.$route.params.uuid}`,
      userAccountUUID:'',
      msg: 'Modify an Employee',
      firstname: 'asd',
      lastname: '',
      jobtitle: '',
      salary: 0.0,
      newDay: '',
      newStartTime: '',
      newEndTime: '',
      schedules: [],
      newJobTitle:'',
      newSalary:'',
    }
  },
  mounted() {
    this.getEmployee()
  },
  methods: {
    getEmployee() {
      authenticatedRequest("GET", `/employee/${this.$route.params.uuid}`, null).then((data) => {
        this.jobtitle = data.jobTitle;
        this.salary = data.salary;
        this.userAccountUUID = data.userAccountUUID;
        this.getUserAccount()
      })
    },
    getUserAccount() {
      authenticatedRequest("GET", `/account/${this.userAccountUUID}`, null).then((data) => {
        this.firstname = data.firstname;
        this.lastname = data.lastname;
        this.getEmployeeSchedule()
      })
    },
    getEmployeeSchedule() {
      authenticatedRequest("GET", `/employee/${this.$route.params.uuid}/schedule`, null).then((data) => {
        this.schedules = data;
      })
    },
    async modifyEmployee(){
      try{
        var response = await authenticatedRequest("PUT", `/employee/${this.$route.params.uuid}`, new Employee(this.newJobTitle, this.newSalary, this.userAccountUUID, this.employee ));
        this.getEmployee()
        alert(`Employee modified successfully`);
      } catch(error){
        alert("An error has occured while modifying employee:"+error.data.error);
      }
    },
    async deleteEmployee(){
      try {
        var response = await authenticatedRequest("DELETE", `/employee/${this.$route.params.uuid}`);
        this.getEmployees();
        alert(`Employee deleted successfully`);
      } catch (error) {
        alert("An error has occured while deleting employee: " + error.data.error);
      }
    },
    createSchedule: function (day, startTime, endTime) {
      if (!this.schedules.some(s => s.day === day)) {
        authenticatedRequest("POST", `/employee/${this.$route.params.uuid}/schedule/`+day, new ScheduleDto(day, startTime, endTime))
          .then((data) => {
            this.getEmployeeSchedule();
          })
          .catch((error) => {
            alert(error);
          })
      } else {
        alert("A schedule for this day already exists!")
      }
      this.newDay = ''
      this.newStartTime = ''
      this.newEndTime = ''
    },
    updateSchedule: function (day, startTime, endTime) {
      authenticatedRequest("PUT", `/employee/${this.$route.params.uuid}/schedule/`+day, new ScheduleDto(day, startTime, endTime))
        .then((data) => {
          this.getEmployeeSchedule();
        })
        .catch((error) => {
          alert(error);
        })
      this.newDay = ''
      this.newStartTime = ''
      this.newEndTime = ''
    },
    deleteSchedule: function (day) {
      if (this.schedules.some(s => s.day === day)) {
        authenticatedRequest("DELETE", `/employee/${this.$route.params.uuid}/schedule/`+day, null)
          .then((data) => {
            this.getEmployeeSchedule();
          })
          .catch((error) => {
            alert(error);
          })
      } else {
        alert("A schedule for this day does not exist!")
      }
      this.newDay = ''
      this.newStartTime = ''
      this.newEndTime = ''
    }
    

  }
}
function Employee(jobTitle, salary, uuid, employeeUuid) {
		this.uuid = employeeUuid
    this.jobTitle = jobTitle
		this.salary = salary
    this.userAccountUUID = uuid;
}

function ScheduleDto (day, startTime, endTime) {
  this.day = day
  this.startTime = startTime
  this.endTime = endTime
}
</script>
<style scoped>
table {
  margin-left: auto;
  margin-right: auto;
}
</style>
