<template>
  <div id="service">
    <h1>Modify vehicle service</h1>
    <br>
    <br>
      <h5> Name: {{ displayName }} <br> </h5>
      <form @submit.prevent="sendRequest">
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
      <input type="submit" value="Update Vehicle Service">
      <br>
      <br>
      <br>
    </form>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";
export default {
  name: 'Service',
  data () {
    return {
      displayName: '',
      duration: '',
      fee: '',
      service: `${this.$route.params.id}`,
      updated:false,
    }
    
  },
  mounted(){
    this.getService();
  },
  methods:{
      async getService(){
        var x = await authenticatedRequest("GET", `/service/${this.$route.params.id}`);
        this.displayName = x.displayName;
        this.updated=true
      },
      async sendRequest(){
        try{
          var response = await authenticatedRequest("PUT", "/service/" + this.$route.params.id,new Service(this.displayName, this.duration, this.fee));
          alert("Vehicle service updated successfully")
        }catch(error) {
          alert("An error has occured while creating a vehicle service: " + error.data.error);
        }
          
        },
        

  }
}

function Service(displayName, duration, fee){
    this.displayName = displayName;
    this.duration = duration;
    this.fee = fee;
  }
</script>



