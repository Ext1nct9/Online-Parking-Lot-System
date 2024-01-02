<template>
  <div id="spot">
    <h1>Parking spot</h1>
      <h3> Parking spot information</h3>
      ID: {{ originalId }} <br>
      Vehicle type: {{ originalVehicleType }} <br>
      Parking spot status: {{ originalParkingSpotStatus }} <br>
      Message: {{ originalMessage }} <br>
    <form @submit.prevent="updateParking">
      <h3> Modify parking spot</h3>
      <label for="vehicleType">Vehicle Type</label>
      <br>
      <select id="size" v-model="vehicleType">
        <option value="regular">Regular</option>
        <option value="large">Large</option>
      </select>
      <br>
      <br>
      <label for="parkingSpotStatus">Parking spot status</label>
      <br>
      <select id="status" v-model="parkingSpotStatus">
        <option value="open">Open</option>
        <option value="reserved">Reserved</option>
        <option value="closed">Closed</option>
      </select>
      <br>
      <br>
      <label for="message">Message </label>
      <br>
      <input type="text" id="message" name="message" v-model="message">
      <br>
      <br>
      <input type="submit" value="Modify parking spot">
      <br>
      <br>
    </form>
    <form @submit.prevent="deleteParking">
      <h3> Delete parking spot</h3>
      <input type="submit" value="Delete parking spot">
      <br>
      <br>
    </form>
  </div>
</template>


<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";

export default {
  name: 'spot',
  data () {
    return {
      originalId: '',
      originalVehicleType: '',
      originalParkingSpotStatus: '',
      originalMessage: '',
      id: '',
      vehicleType: "regular",
      parkingSpotStatus: "open",
      message: ''
      
    }
  },
  mounted(){
    this.getParking();
  },
  methods:{
      async getParking(){
          var response = await authenticatedRequest("GET", `/spot/${this.$route.params.id}`);
          this.originalId = response.id;
          this.originalVehicleType = response.vehicleType;
          this.originalParkingSpotStatus = response.parkingSpotStatus;
          this.originalMessage = response.message;
        },
      async updateParking(){
        try{
          var response = await authenticatedRequest("PUT", `/spot/${this.$route.params.id}`, new updateParkingSpot(this.originalId, this.vehicleType, this.parkingSpotStatus, this.message));
          this.getParking();
          alert("Parking spot updated successfully")
        }catch(error) {
          alert("An error has occured while updating a parking spot: " + error.data.error);
        }
      },
      async deleteParking(){
        try{
          var response = await authenticatedRequest("DELETE", `/spot/${this.$route.params.id}`);
          alert(`Parking spot deleted successfully`);
        }catch(error) {
          alert("An error has occured while deleting a parking spot: " + error.data.error);
        }
      }
        
  }
}

function updateParkingSpot(id, vehicleType, parkingSpotStatus, message){
    this.id = id;
    this.vehicleType = vehicleType;
    this.parkingSpotStatus = parkingSpotStatus;
    this.message = message;
  }
</script>
