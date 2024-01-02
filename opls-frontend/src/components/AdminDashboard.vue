<template>
  <div id="admindashboard">
    <p></p>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); grid-gap: 10px;">
      <div v-for="action in actions">
        <router-link :to="`${action.link}`">{{ action.displayName }}</router-link>
      </div>
    </div>
    <p></p>
    <h1>Administration Dashboard</h1>
    <p></p>
    <h2>Parking Lot System Configuration</h2>
    Increment fee: {{ configuration.incrementFee }} <br>
    Monthly fee: {{ configuration.monthlyFee }} <br>
    Increment time: {{ configuration.incrementTime }} <br>
    Monthly time: {{ configuration.maxIncrementTime }} <br>
    <p></p>
    <h2>Update Configuration</h2>
    Increment fee: <input v-model.number="newIncrementFee" placeholder=configuration.incrementFee> <br>
    Monthly fee: <input v-model.number="newMonthlyFee" placeholder=configuration.monthlyFee> <br>
    Increment time: <input v-model.number="newIncrementTime" placeholder=configuration.incrementTime> <br>
    Monthly time: <input v-model.number="newMaxIncrementTime" placeholder=configuration.maxIncrementTime>  <br>
    <button v-bind:disabled="!newIncrementFee || !newMonthlyFee || !newIncrementTime || !newMaxIncrementTime"
            @click="updateConfiguration(newIncrementFee, newMonthlyFee, newIncrementTime, newMaxIncrementTime)">Update Config</button>
    <p></p>
    <h2>Parking Lot Schedules</h2>
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
  </div>
</template>

<script src="./admin_dashboard.js"></script>

<style scoped>
table {
  margin-left: auto;
  margin-right: auto;
}
</style>
