<template>
  <div id="newservice">
    <h1>Create a new vehicle service</h1>
      <form @submit.prevent="sendRequest">
      <label for="displayName">Name</label>
      <br>
      <input type="text" id="displayName" name="displayName" v-model="displayName">
      <br>
      <br>
      <label for="duration">Duration</label>
      <br>
      <input type="text" id="duration" name="duration" v-model="duration">
      <br>
      <br>
      <label for="fee">Fee</label>
      <br>
      <input type="text" id="fee" name="fee" v-model="fee">
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
      displayName: '',
      duration: '',
      fee: '',
    }
  },
  methods:{
      async sendRequest(){
        try{
          var response = await authenticatedRequest("POST", "/service",new Service(this.displayName, this.duration, this.fee));
          alert("Vehicle Service created successfully")
        }catch(error) {
          alert("An error has occured while creating a vehicle service: " + error.data.error);
        }
        }
        

  }
}

function Service(displayName, duration, fee){
    this.displayName = displayName;
    this.duration = duration;
    this.fee = fee;
  }
</script>



