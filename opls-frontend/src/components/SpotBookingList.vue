<template>
  <div class="hello">
    <h1>Parking Spot Bookings</h1>
    <p> Search by 
      <select id="searchType" v-model="searchType">
        <option value="uuid">Uuid</option>
        <option value="status">Status</option>
        <option value="confirmationNumber">Confirmation Number</option>
        <option value="spotId">Spot Id</option>
      </select>
      <select id="statusSelect" v-model="statusSelect" v-if="searchType == 'status'">
        <option value="REQUESTED">Requested</option>
        <option value="PAID">Paid</option>
        <option value="CONFIRMED">Confirmed</option>
      </select>
      <input type="text" id="searchBar" :placeholder= "searchType" v-model="searchValue" v-if="searchType != 'status'">
      <button @click=" getBookings()" v-if="searchType != null">Search</button>
    </p>
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
import { store } from "../store";

export default {
  name: 'spotBookingList',
  data () {
    return {
      msg: 'Parking Spot Bookings',
      searchType: "confirmationNumber",
      statusSelect: "requested",
      searchValue: "",
      bookings: [],
      spotids: {}
    }
  },
  methods:{
    async getBookings(){
      if (this.searchType == "uuid"){
        var response = await authenticatedRequest("GET", `/spot/booking/${this.searchValue}`);
      }
      else if (this.searchType == "status"){
        var response = await authenticatedRequest("GET", `/spot/booking/byStatus/${this.statusSelect}`);
      }
      else if (this.searchType == "confirmationNumber"){
        var response = await authenticatedRequest("GET", `/spot/booking/byConfirmation/${this.searchValue}`);
      }
      else if (this.searchType == "spotId"){
        var response = await authenticatedRequest("GET", `/spot/${this.searchValue}/booking`);
      }
      if (Array.isArray(response)){
        this.bookings = response;
      }
      else{
        this.bookings = [response];
      }
    },
    async confirmBooking(uuid){
    if (this.spotids[uuid] != null){
      try {
        var response = await authenticatedRequest("PATCH", `/spot/booking/${uuid}`, new patchRequest("CONFIRMED", this.spotids[uuid]));
        this.getBookings();
        alert("Booking Confirmed with spot id: " + this.spotids[uuid]);
      } catch (error) {
        alert("an error has occured: " + error.data.error);
      }

    }
    else{
      alert("Please enter a spot id");
    }
  }
  },
}
function patchRequest(status, parkingSpotId){
  this.status = status;
  this.parkingSpotId = parkingSpotId;
}
</script>
