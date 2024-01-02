<template>
  <div class="hello">
    <h1>Vehicle Service Bookings</h1>
    <form v-if="bookingToModify.selected">
      <h3>Modify:</h3>
      <label>Confirmation number:</label>
      <input type="text" v-model="bookingToModify.confirmationNumber" disabled />
      <label>License plate:</label>
      <input type="text" v-model="bookingToModify.licensePlate" />
      <button @click="modifyBooking">Submit changes</button>
    </form>
    <form>
      <h3>Filter:</h3>
      <label>Services:</label>
      <select multiple v-model="queryReq.vehicleServiceIds">
        <option v-for="service in services" v-bind:value="service.id">{{ service.displayName }}</option>
      </select>
      <button @click="loadBookings">Refresh</button>
    </form>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); grid-gap: 10px;">
      <div v-for="booking in bookings" style="border: 1px solid #000;">
        <p v-if="booking.uuid">Uuid: {{ booking.uuid }}</p>
        <p v-if="booking.name">Service: {{ booking.name }}</p>
        <p>Start: {{ booking.startDate }}</p>
        <p>End: {{ booking.endDate }}</p>
        <p>Duration: {{ booking.durationMinutes }} minutes</p>
        <button v-if="canUpdate" @click="$event => setBookingToModify(booking.uuid)">View Booking</button>
        <button v-if="canUpdate" @click="$event => completeService(booking.uuid)">Complete Service</button>
        <button v-if="canUpdate" @click="$event => deleteBooking(booking.uuid)">Delete Booking</button>
      </div>
    </div>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";
import { claims } from "../router/routes";
import { BookingStatus, ServiceBookingQueryRequestDto, UpdateServiceBookingRequestDto } from "../dto/serviceBooking";
import { getDate, toDateString } from "../utils/dateHelper";

export default {
  name: 'serviceBookings',
  data () {
    return {
      msg: 'Service Bookings',
      services: this.loadServices(),
      bookings: this.loadBookings(),
      queryReq: {
        own: false,
        vehicleServiceIds: [],
        startDate: getDate(),
        endDate: new Date(getDate().getTime() + 30 * 60 * 1000),
      },
      bookingToModify: {
        selected: false,
        uuid: undefined,
        licensePlate: undefined,
        bookingStatus: undefined,
        confirmationNumber: undefined,
      },
      canUpdate: store.hasClaim(claims.admin, claims.employee),
    }
  },
  methods: {
    loadServices() {
      authenticatedRequest("GET", "/service")
        .then((data) => {
          let list = [];
          for (var service of data) {
            list.push({
              displayName: service.displayName,
              id: service.id
            });
          }
          this.services = list;
          return list;
        })
        .catch((err) => []);
    },
    loadBookings() {
      // parse input
      let req = this.queryReq || ServiceBookingQueryRequestDto();
      if (typeof req.startDate !== "string") {
        req.startDate = toDateString(req.startDate);
      }
      if (typeof req.endDate !== "string") {
        req.endDate = toDateString(req.endDate);
      }
      
      // request
      authenticatedRequest("POST", "/service/booking/search", req)
        .then((data) => {
          this.bookings = data.bookings;

          for (var booking of this.bookings) {
            booking.startDate = toDateString(new Date(booking.startDate));
            booking.endDate = toDateString(new Date(booking.endDate));
          }

          return data.bookings;
        })
        .catch((err) => {
          alert(err);
          return [];
        });
    },
    setBookingToModify(uuid) {
      authenticatedRequest("GET", "/service/booking/" + uuid)
        .then((data) => {
          this.bookingToModify.selected = true;
          this.bookingToModify.uuid = uuid;
          this.bookingToModify.licensePlate = data.licensePlate;
          this.bookingToModify.bookingStatus = data.bookingStatus;
          this.bookingToModify.confirmationNumber = data.confirmationNumber;
        })
        .catch((err) => {
          alert(err);
        });
    },
    modifyBooking() {
      let req = UpdateServiceBookingRequestDto(this.bookingToModify.licensePlate, this.bookingToModify.bookingStatus);
      authenticatedRequest("PATCH", "/service/booking/" + this.bookingToModify.uuid, req)
        .then((data) => {
          alert("Booking modified");
        })
        .catch((err) => {
          alert(err);
        });
    },
    completeService(uuid) {
      let req = UpdateServiceBookingRequestDto(undefined, BookingStatus.completed);
      authenticatedRequest("PATCH", "/service/booking/" + uuid, req)
        .then((data) => {
          this.bookings = this.bookings.filter((value) => value.uuid !== uuid);
        })
        .catch((err) => {
          alert(err);
        });
    },
    deleteBooking(uuid) {
      authenticatedRequest("DELETE", "/service/booking/" + uuid)
        .then((data) => {
          this.bookings = this.bookings.filter((value) => value.uuid !== uuid);
        })
        .catch((err) => {
          alert(err);
        });
    },
  },
}
</script>
