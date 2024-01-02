<template>
  <div class="hello">
    <h1>Create a Vehicle Service Booking</h1>
    <div v-if="!paid">
    <br>
    <br>
    <h5> Name: {{displayName}}<br></h5>
      <form @submit.prevent="sendRequest">
      <label for="licensePlate">License Plate</label>
      <br>
      <input type="text" id="licensePlate" name="licensePlate" placeholder="123 ABC" v-model="req.licensePlate">
      <br>
      <br>
      <label for="creditCardNumber">Credit Card Number</label>
      <br>
      <input type="text" id="creditCardNumber" name="creditCardNumber" placeholder="1234 4567 8910 1112" v-model="req.creditCardNumber">
      <br>
      <br>
      <label for="startDate">Start Date</label>
      <br>
      <input type="text" id="startDate" name= "startDate" placeholder= "" v-model="req.startDate">
      <br>
      <br>
      <button onclick="sendRequest()">Create Booking</button>
      <br>
      <br>
    </form>
    </div>
      <div v-html="transactionSummary" v-if="paid">
      </div>
      <button @click="$event => paid = false" v-if="paid">Make Another Booking</button>
      <br>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";
import { getDate, toDateString } from "./../utils/dateHelper";
import { BookingStatus, ServiceBookingRequestDto } from "../dto/serviceBooking";


export default {
  name: 'newServiceBooking',
  data () {
    return {
      displayName:"",
      transactionSummary:"",
      paid:false,
      vehicleServiceId:`${this.$route.params.serviceId}`,
      req: {
      creditCardNumber:"",
      startDate: getDate(),
      licensePlate:""
      }
    }
  },
  mounted(){
    this.getService()
  },
  methods:{
      getService(){
        authenticatedRequest("GET", `/service/${this.$route.params.serviceId}`).then((data) => {
        this.displayName = data.displayName;
        })
      },
      async sendRequest(){
       try{
        let req = this.req || ServiceBookingRequestDto();
        if (typeof req.startDate !== "string") {
          req.startDate = toDateString(req.startDate);
        }
         var response = await authenticatedRequest("POST",`/service/${this.$route.params.serviceId}/booking`,req);
         if (response){
            this.transactionSummary = `<p><b>Transaction Summary</b><br>
              <br> Confirmation Number : ${response.confirmationNumber} <br>
              <br> Cost : ${response.cost} $ <br> 
              <br> Service : ${response.vehicleServiceId} <br>
              <br> Start Date: ${response.startDate} <br>
              <br> End Date: ${response.endDate} <br>
              </p>`;
            this.paid = true;
          }
        } catch (error) {
          alert(`An error has occurred:`+error.data.error);
        }
      }
    }
  }
</script>
