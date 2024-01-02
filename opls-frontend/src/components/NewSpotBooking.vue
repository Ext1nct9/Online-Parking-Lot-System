<template>
  <div class="hello">
    <h1>Create a Parking Spot Booking</h1>
    <div v-if="!paid">
    <input type="checkbox" id="monthly" v-model="monthly">Monthly</input>
    <form v-if="!monthly" @submit.prevent="sendRequest">
      <label for="spotId">SpotId</label>
      <br>
      <input type="text" id="spotId" name="spotId" placeholder="Fxxx" v-model="spotId">
      <br>
      <br>
      <label for="creditCard">Credit Card</label>
      <br>
      <input type="text" id="creditCard" name="creditCard" placeholder="1234 4567 8910 1112" v-model="creditCard">
      <br>
      <br>
      <label for="duration">Duration (Minutes)</label>
      <br>
      <input type="text" id="duration" name="duration" placeholder="30" v-model="duration">
      <br>
      <br>
      <label for="licensePlate">License Plate</label>
      <br>
      <input type="text" id="licensePlate" name="licensePlate" placeholder="123 ABC" v-model="licensePlate">
      <br>
      <br>
      <label for="VehicleType">Vehicle Type</label>
      <br>
      <select id="size" v-model="VehicleType">
        <option value="regular">Regular</option>
        <option value="large">Large</option>
      </select>
      <br>
      <br>
      <input type="submit" value="Request Booking">
    </form>
    <form v-else @submit.prevent="sendRequest">
      <label for="creditCard">Credit Card</label>
      <br>
      <input type="text" id="creditCard" name="creditCard" placeholder="1234 4567 8910 1112" v-model="creditCard">
      <br>
      <br>
      <label for="licensePlate">License Plate</label>
      <br>
      <input type="text" id="licensePlate" name="licensePlate" placeholder="123 ABC" v-model="licensePlate">
      <br>
      <br>
      <label for="VehicleType">Vehicle Type</label>
      <br>
      <select id="size" v-model="VehicleType">
        <option value="regular">Regular</option>
        <option value="large">Large</option>
      </select>
      <br>
      <br>
      <input type="submit" value="Request Monthly Booking">
    </form>
    </div>
    <div v-html="transactionSummary" v-if="paid">

    </div>
    <button @click="paid = false" v-if="paid">Make Another Booking</button>
  </div>
</template>


<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";

export default {
  name: 'newSpotBooking',
  data () {
    return {
      spotId: "",
      creditCard: "",
      duration: "",
      licensePlate: "",
      VehicleType: "regular",
      monthly: false,
      transactionSummary: "",
      paid: false
    }
  },
  methods:{
      async sendRequest(){
        if(this.monthly){
          try {
            var response = await authenticatedRequest("POST", "/spot/booking/monthly",new MonthlySpotBooking(this.creditCard, this.licensePlate, this.VehicleType));
            if (response){
              this.transactionSummary = `<p><b>Transaction Summary</b><br>
                <br> Confirmation Number : ${response.confirmationNumber} <br>
                <br> Cost : ${response.cost} $ <br> 
                <br> Spot : You will be assigned a spot by an employee shortly. <br>
                <br> Start Date: ${response.startDate} <br>
                <br> End Date: ${response.endDate} <br>
                </p>`;
                this.paid = true;
            }
          } catch (error) {
            alert(`An error has occured : ${error.data.error}`);
          }
          
          
        }
        else{
          try {
            var response = await authenticatedRequest("POST", "/spot/booking/incremental", new IncrementalSpotBooking(this.spotId, this.creditCard, this.duration, this.licensePlate, this.VehicleType));
            if (response){
              this.transactionSummary = `<p><b>Transaction Summary</b><br>
                <br> Confirmation Number : ${response.confirmationNumber} <br>
                <br> Cost : ${response.cost} $ <br> 
                <br> Spot : ${response.parkingSpotId} <br>
                <br> Start Date: ${response.startDate} <br>
                <br> End Date: ${response.endDate} <br>
                </p>`;
                this.paid = true;
            }
          } catch (error) {
            alert(`An error has occured : ${error.data.error}`);
          }
        
        }
      }
  }
}
function IncrementalSpotBooking(spotId, creditCard, duration, licensePlate, VehicleType){
    this.parkingSpotId = spotId;
    this.creditCardNumber = creditCard;
    this.duration = duration;
    this.licensePlate = licensePlate;
    this.vehicleType = VehicleType;
  }
function MonthlySpotBooking(creditCard, licensePlate, VehicleType){
    this.creditCardNumber = creditCard;
    this.licensePlate = licensePlate;
    this.vehicleType = VehicleType;
  }
</script>
