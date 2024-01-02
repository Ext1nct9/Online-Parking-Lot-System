<template>
  <div class="hello">
    <h1>Account Information</h1>
    <p></p>
    <h2>Your Profile</h2>
    Username: {{ currentUser.username }} <br>
    First name: {{ currentUser.firstName }} <br>
    Last name: {{ currentUser.lastName }} <br>
    <div v-if="currentCustomer">
        Billing account ID: {{ currentCustomer.billingAccountId }} <br>
        Saved license plate: {{ currentCustomer.savedLicensePlate }} <br>
    </div>

    
    <p></p>
    <h2>Update Your Profile</h2>
    <h4>Personal information </h4>
    Username: <input type="text" v-model.trim="newUsername" > <br>
    First name: <input type="text" v-model.trim="newFirstName" > <br>
    Last name: <input type="text" v-model.trim="newLastName" > <br>
    <button v-bind:disabled="!newUsername || !newFirstName || !newLastName "
            @click="updateAccount(newUsername, newFirstName, newLastName)">Update Account</button>
            <p></p>

    
            <h4>Customer information</h4>
    Billing account id: <input type="text" v-model.trim="newBillingAccountId" > <br>
    License Plate: <input type="text" v-model.trim="newSavedLicensePlate" > <br>
    <button v-bind:disabled="!newBillingAccountId || !newSavedLicensePlate "
            @click="updateCustomer(newBillingAccountId, newSavedLicensePlate)">{{ !!currentCustomer ? "Update customer" : "Create customer" }}</button>
            <p></p>
            <h4>New password</h4>
    New password: <input type="text" v-model.trim="newPass" > <br>
    Confirm answer to security question: {{ currentUser.securityQuestion }} <br>
    Security answer: <input type="text" v-model.trim="oldA" > <br>
    <button v-bind:disabled="!newPass || !oldA "
            @click="updatePassword(newPass, currentUser.securityQuestion, oldA)">Update password</button>
    <p></p>


    <h4>New security question and answer</h4>
    Security question: <input type="text" v-model.trim="newQuestion" > <br>
    Security answer: <input type="text" v-model.trim="newAnswer" > <br>
    Confirm password:<input type="text" v-model.trim="oldPass" > <br>
    <button v-bind:disabled="!newQuestion || !newAnswer || !oldPass "
            @click="updateSecurity(newQuestion, newAnswer, oldPass)">Update security</button>
    <p></p>

  
  </div>
</template>

<script src="./accountDashboard.js"></script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";

