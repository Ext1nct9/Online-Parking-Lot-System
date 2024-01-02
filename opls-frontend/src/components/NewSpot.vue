<template>
  <div id="newspot">
    <h1>Create a new parking spot</h1>
      <form @submit.prevent="sendRequest">
      <label for="id">4 Character id </label>
      <br>
      <input type="text" id="id" name="id" placeholder="Axxx" v-model="id">
      <br>
      <br>
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
      <input type="submit" value="Create Vehicle Service">
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
      id: '',
      vehicleType: "regular",
      parkingSpotStatus: "open",
      message: ''
    }
  },
  methods:{
      async sendRequest(){
        try{
          var response = await authenticatedRequest("POST", "/spot",new Spot(this.id, this.vehicleType, this.parkingSpotStatus, this.message));
          alert("Parking spot created successfully")
        }catch(error) {
          alert("An error has occured while creating a parking spot: " + error.data.error);
        }
        }
        

  }
}

function Spot(id, vehicleType, parkingSpotStatus, message){
    this.id = id;
    this.vehicleType = vehicleType;
    this.parkingSpotStatus = parkingSpotStatus;
    this.message = message;
  }
</script>





