<template>
  <div class="hello">
    <h1>Parking Spots</h1>
    <form>
      <h3>Filter:</h3>
      <label>Unbooked:</label>
      <input type="checkbox" v-model="queryReq.unbooked" />
      <label>Floors:</label>
      <input type="text" v-model="queryReq.floors" />
      <label>Statuses:</label>
      <select multiple v-model="queryReq.statuses">
        <option value="open">Open</option>
        <option value="reserved">Reserved</option>
        <option value="closed">Closed</option>
      </select>
      <label>Sizes:</label>
      <select multiple v-model="queryReq.sizes">
        <option value="regular">Regular</option>
        <option value="large">Large</option>
      </select>
      <button @click="loadSpots">Refresh</button>
    </form>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Size</th>
          <th>Status</th>
          <th>Message</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="spot in spots" :key="spot.id">
          <td>{{ spot.id }}</td>
          <td>{{ spot.vehicleType }}</td>
          <td>{{ spot.parkingSpotStatus }}</td>
          <td>{{ spot.message }}</td>
          <td>
            <button v-if="spot.isOpen" @click="bookSpot(spot.id)">Book</button>
            <button v-if="canUpdate" @click="updateSpot(spot.id)">Update</button>
            <button v-if="canDelete" @click="deleteSpot(spot.id)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { authenticatedRequest } from "./apiclient";
import { store } from "../store";
import { claims, routes } from "../router/routes";
import { ParkingSpotQueryRequestDto, ParkingSpotStatus, VehicleType } from "../dto/spot";

export default {
  name: 'parkingSpots',
  data () {
    return {
      msg: 'Parking Spots',
      spots: this.loadSpots(),
      queryReq: {
        unbooked: false,
        floors: "",
        statuses: [ParkingSpotStatus.open],
        sizes: [VehicleType.regular],
      },
      canUpdate: store.hasClaim(claims.admin, claims.employee),
      canDelete: store.hasClaim(claims.admin)
    }
  },
  methods: {
    loadSpots() {
      // parse input
      let req = this.queryReq || ParkingSpotQueryRequestDto();
      if (typeof req.floors === "string") {
        req.floors = req.floors.split("");
      }

      authenticatedRequest("POST", "/spot/search", req)
        .then((data) => {
          for (var spot of data.parkingSpots) {
            spot.isOpen = spot.parkingSpotStatus === ParkingSpotStatus.open;
          }
          this.spots = data.parkingSpots;
          return data.parkingSpots;
        })
        .catch((err) => {
          alert(err);
          return [];
        });
    },
    bookSpot(id) {
      this.$router.push({
        path: routes.NEW_SPOT_BOOKING(),
        query: {
          "spot": id
        }
      });
    },
    updateSpot(id) {
      this.$router.push({
        path: routes.PARKING_SPOT_INFORMATION(id)
      });
    },
    deleteSpot(id) {
      authenticatedRequest("DELETE", "/spot/" + id)
        .then((data) => {
          this.spots = this.spots.filter((value) => value.id !== id);
        })
        .catch((err) => {
          alert(err);
        });
    }
  },
}
</script>
