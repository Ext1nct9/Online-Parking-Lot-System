<template>
  <div class="hello">
    <h1>Service List</h1>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); grid-gap: 10px;">
      <div v-for="s in services" style = "border: 1px solid #000;">
        <router-link :to="`/service/${s.displayName}`">{{s.displayName}}</router-link>
        <p>Fee: {{s.fee}}</p>
        <p>Duration: {{s.duration}}</p>
        <button @click="deleteService(s.id)">Delete Service</button>
      </div>
    </div>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";


export default {
  name: 'serviceList',
  data () {
    return {
      services: [],
    }
  },
  mounted(){
    this.getServices();
  },
  methods:{
    async getServices(){
      try {
        var response = await authenticatedRequest("GET", `/service`);
        if (Array.isArray(response)){
          this.services = response;
        }
        else{
          this.services = [response];
        }
       
      } catch (error) {
        alert("An unexpected error has occured while fetching services: " + error.data.error);
      }
    },
    async deleteService(id){
      try {
        var response = await authenticatedRequest("DELETE", `/service/${id}`);
        this.getServices();
        alert(`Service ${id} deleted successfully`);
      } catch (error) {
        alert("An error has occured while deleting service: " + error.data.error);
      }
    },
  },
}
</script>
