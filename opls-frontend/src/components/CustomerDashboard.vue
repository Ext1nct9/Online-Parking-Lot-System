<template>
  <div class="hello"> 
    <p></p>
    <router-link :to="`${actions.bookSpot.link}`">{{ actions.bookSpot.displayName }}</router-link>
    <router-link :to="`${actions.bookService.link}`">{{ actions.bookService.displayName }}</router-link>
    <h1>Customer Dashboard</h1>
    <p></p>
    <h2>Your Profile</h2>
    First name: {{ firstname }} <br>
    Last name: {{ lastname }} <br>
    Billing account ID: {{ billingAccountId }} <br>
    Saved license plate: {{ licensePlate }} <br>
    <p></p>
  
    <router-link :to="`${actions.updateAccount.link}`">{{ actions.updateAccount.displayName }}</router-link>
    <p></p>
    <h2>Your Bookings</h2>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); grid-gap: 10px;">
      <div v-for="v in bookings" style = "border: 1px solid #000;">
        <p>confirmationNumber: {{v.confirmationNumber}}</p>
        <p>cost: {{v.cost}}</p>
        <p>startDate: {{v.startDate}}</p>
        <p>endDate: {{v.endDate}}</p>
        <p>status: {{v.status}}</p>
        <p v-if="v.parkingSpotId != null">spotId: {{v.parkingSpotId}}</p>
        <p v-if="v.parkingSpotId == null">spotId: <input type="text" placeholder= "spot Id" v-model="spotids[v.uuid]"> <button @click=" confirmBooking(v.uuid)">Confirm Booking</button></p>
        <p>uuid: {{v.uuid}}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { routes } from "../router/routes";

export default {
  name: 'customerDashboard',
  data () {
    return {
      msg: 'Customer Dashboard',
      firstname: '',
      lastname: '',
      billingAccountId: '',
      licensePlate: '',
      bookings: [],
      actions: {
        bookSpot: {
          link: routes.NEW_SPOT_BOOKING(),
          displayName: "Book a spot"
        },
        bookService: {
          link: routes.NEW_SERVICE_BOOKING(),
          displayName: "Book a vehicle service",
        },
        updateAccount: {
          link: routes.ACCOUNT_INFORMATION(),
          displayName: "Update account details",
        }
      },
    }
  },
  mounted() {
    this.getCustomer()
    this.getUserAccount()
    this.getCustomerBookings()
  },
  methods: {
    getCustomer() {
      authenticatedRequest("GET", "/customer", null).then((data) => {
        this.billingAccountId = data.billingAccountId;
        this.licensePlate = data.savedLicensePlate;
      })
    },
    getUserAccount() {
      authenticatedRequest("GET", "/account", null).then((data) => {
        this.firstname = data.firstname;
        this.lastname = data.lastname;
      })
    },
    getCustomerBookings() {
      authenticatedRequest("GET", "/customer/spot/booking", null).then((response) => {
        if (Array.isArray(response)){
        this.bookings = response;
      }
      else{
        this.bookings = [response];
      }
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
