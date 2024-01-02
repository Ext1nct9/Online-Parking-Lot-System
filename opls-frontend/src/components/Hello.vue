<template>
  <div class="hello">
    <h1>{{ msg }}</h1>

    <p>
      We currently have {{ numberOfSpots }} open parking spots!
      <button @click="getNumberOfSpots">Refresh</button>
    </p>

    <table>
      <tbody>
        <tr>
          <td>
            <form>
              <p>Find your parking spot booking:</p>
              <label>Confirmation number:</label>
              <input type="text" v-model="spotBookingConfirmationNumber" />
              <button @click="getSpotBooking">Search</button>
            </form>
          </td>
          <td>
            <form>
              <p>Find your vehicle service booking:</p>
              <label>Confirmation number:</label>
              <input type="text" v-model="serviceBookingConfirmationNumber" />
              <button @click="getServiceBooking">Search</button>
            </form>
          </td>
        </tr>
        <tr v-if="fetchedSpotBooking !== undefined || fetchedServiceBooking !== undefined">
          <td>
            <div v-if="!!fetchedSpotBooking">
              <p>Parking spot: {{ fetchedSpotBooking.parkingSpotId }}</p>
              <p>Status: {{ fetchedSpotBooking.status }}</p>
              <p>From: {{ fetchedSpotBooking.startDate }}</p>
              <p>To: {{ fetchedSpotBooking.endDate }}</p>
            </div>
            <p v-if="fetchedSpotBooking === false" style="color: red">Not found</p>
          </td>
          <td>
            <div v-if="fetchedServiceBooking">
              <p>Vehicle Service: {{ fetchedServiceBooking.vehicleServiceId }}</p>
              <p>Status: {{ fetchedServiceBooking.status }}</p>
              <p>From: {{ fetchedServiceBooking.startDate }}</p>
              <p>To: {{ fetchedServiceBooking.endDate }}</p>
            </div>
            <p v-if="fetchedServiceBooking === false" style="color: red">Not found</p>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { ParkingSpotStatus, ParkingSpotQueryRequestDto } from "./../dto/spot";

export default {
  name: 'hello',
  data () {
    return {
      msg: "Welcome to the online parking lot system!",
      numberOfSpots: this.getNumberOfSpots(),
      spotBookingConfirmationNumber: "",
      serviceBookingConfirmationNumber: "",
      fetchedSpotBooking: undefined,
      fetchedServiceBooking: undefined,
    }
  },
  methods: {
    getNumberOfSpots() {
      authenticatedRequest("POST", "/spot/search/count", 
        ParkingSpotQueryRequestDto(true, [], [ ParkingSpotStatus.open ]))
        .then((data) => {
          this.numberOfSpots = data.count;
          return data.count;
        })
        .catch((err) => {
          return 0;
        });
    },
    getSpotBooking() {
      authenticatedRequest("GET", "/spot/booking/byConfirmation/" + this.spotBookingConfirmationNumber)
        .then((data) => {
          this.fetchedSpotBooking = {
            status: data.status,
            parkingSpotId: data.parkingSpotId,
            startDate: data.startDate,
            endDate: data.endDate,
          };
        })
        .catch((err) => {
          this.fetchedSpotBooking = false;
        });
    },
    getServiceBooking() {
      authenticatedRequest("GET", "/service/booking/byConfirmation/" + this.serviceBookingConfirmationNumber)
        .then((data) => {
          this.fetchedServiceBooking = {
            status: data.status,
            vehicleServiceId: data.vehicleServiceId,
            startDate: data.startDate,
            endDate: data.endDate,
          };
        })
        .catch((err) => {
          this.fetchedServiceBooking = false;
        });
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1, h2 {
  font-weight: normal;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: inline-block;
  margin: 0 10px;
}

a {
  color: #42b983;
}
</style>
