<template>
  <div class="employeedashboard">
    <p></p>
    <router-link :to="`${actions.spotBookings.link}`">{{ actions.spotBookings.displayName }}</router-link>
    <router-link :to="`${actions.serviceBookings.link}`">{{ actions.serviceBookings.displayName }}</router-link>
    <p></p>
    <h1>Employee Dashboard</h1>
    <p></p>
    <h2>Your Profile</h2>
    First name: {{ firstname }} <br>
    Last name: {{ lastname }} <br>
    Job title: {{ jobtitle }} <br>
    Salary: {{ salary }} <br>
    <router-link :to="`${actions.updateAccount.link}`">{{ actions.updateAccount.displayName }}</router-link>
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
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { routes } from "../router/routes";

export default {
  name: 'EmployeeDashboard',
  data () {
    return {
      msg: 'Employee Dashboard',
      firstname: '',
      lastname: '',
      jobtitle: '',
      salary: 0.0,
      schedules: [],
      actions: {
        spotBookings: {
          link: routes.SPOT_BOOKING_LIST(),
          displayName: "Parking spot bookings"
        },
        serviceBookings: {
          link: routes.SERVICE_BOOKING_LIST(),
          displayName: "Vehicle service bookings",
        },
        updateAccount: {
          link: routes.ACCOUNT_INFORMATION(),
          displayName: "Update account details",
        }
      },
    }
  },
  mounted() {
    this.getEmployee()
    this.getUserAccount()
    this.getEmployeeSchedule()
  },
  methods: {
    getEmployee() {
      authenticatedRequest("GET", "/employee", null).then((data) => {
        this.jobtitle = data.jobTitle;
        this.salary = data.salary;
      })
    },
    getUserAccount() {
      authenticatedRequest("GET", "/account", null).then((data) => {
        this.firstname = data.firstname;
        this.lastname = data.lastname;
      })
    },
    getEmployeeSchedule() {
      authenticatedRequest("GET", "/employee/schedule", null).then((data) => {
        this.schedules = data;
      })
    }
  }
}
</script>

<style scoped>
table {
  margin-left: auto;
  margin-right: auto;
}
</style>
