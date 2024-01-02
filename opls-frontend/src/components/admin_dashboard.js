function OplsConfigurationDto (monthlyFee, incrementFee, incrementTime, maxIncrementTime) {
  this.monthlyFee = monthlyFee
  this.incrementFee = incrementFee
  this.incrementTime = incrementTime
  this.maxIncrementTime = maxIncrementTime
}

function ScheduleDto (day, startTime, endTime) {
  this.day = day
  this.startTime = startTime
  this.endTime = endTime
}

import { authenticatedRequest } from "./apiclient";
import { routes } from "./../router/routes";

export default {
  name: "AdminDashboard",
  data() {
    return {
      configuration: '',
      newIncrementFee: 0,
      newMonthlyFee: 0,
      newIncrementTime: 0,
      newMaxIncrementTime: 0,
      schedules: [],
      newDay: '',
      newStartTime: '',
      newEndTime: '',
      actions: [{
        link: routes.NEW_PARKING_SPOT(),
        displayName: "Create parking spot",
      }, {
        link: routes.PARKING_SPOT_LIST(),
        displayName: "Modify parking spots",
      }, {
        link: routes.SPOT_BOOKING_LIST(),
        displayName: "Parking spot bookings",
      }, {
        link: routes.NEW_SERVICE(),
        displayName: "Create vehicle service",
      }, {
        link: routes.SERVICE_LIST(),
        displayName: "Modify vehicle services",
      }, {
        link: routes.SERVICE_BOOKING_LIST(),
        displayName: "Vehicle service bookings",
      }, {
        link: routes.NEW_EMPLOYEE(),
        displayName: "Create employee",
      }, {
        link: routes.EMPLOYEE_LIST(),
        displayName: "Modify employees",
      }],
    }
  },
  mounted() {
    this.getConfiguration()
    this.getConfigurationSchedules()
  },
  methods: {
    getConfiguration() {
      authenticatedRequest("GET", "/config", null).then((data) => {
        this.configuration = new OplsConfigurationDto(
          data.monthlyFee, data.incrementFee, data.incrementTime, data.maxIncrementTime);
      })
    },
    getConfigurationSchedules() {
      authenticatedRequest("GET", "/config/schedule", null).then((data) => {
        this.schedules = data;
      })
    },
    updateConfiguration: function (incrementFee, monthlyFee, incrementTime, maxIncrementTime) {
      authenticatedRequest("PUT", "/config",
        new OplsConfigurationDto(monthlyFee, incrementFee, incrementTime, maxIncrementTime))
        .then((data) => {
        this.configuration = new OplsConfigurationDto(
          data.monthlyFee, data.incrementFee, data.incrementTime, data.maxIncrementTime);
        })
        .catch((error) => {
          alert(error);
        })
      this.newIncrementFee = 0
      this.newMonthlyFee = 0
      this.newIncrementTime = 0
      this.newMaxIncrementTime = 0
    },
    createSchedule: function (day, startTime, endTime) {
      if (!this.schedules.some(s => s.day === day)) {
        authenticatedRequest("POST", "/config/schedule/"+day, new ScheduleDto(day, startTime, endTime))
          .then((data) => {
            this.getConfigurationSchedules();
          })
          .catch((error) => {
            alert(error);
          })
      } else {
        alert("A schedule for this day already exists!")
      }
      this.newDay = ''
      this.newStartTime = ''
      this.newEndTime = ''
    },
    updateSchedule: function (day, startTime, endTime) {
      authenticatedRequest("PUT", "/config/schedule/"+day, new ScheduleDto(day, startTime, endTime))
        .then((data) => {
          this.getConfigurationSchedules();
        })
        .catch((error) => {
          alert(error);
        })
      this.newDay = ''
      this.newStartTime = ''
      this.newEndTime = ''
    },
    deleteSchedule: function (day) {
      if (this.schedules.some(s => s.day === day)) {
        authenticatedRequest("DELETE", "/config/schedule/"+day, null)
          .then((data) => {
            this.getConfigurationSchedules();
          })
          .catch((error) => {
            alert(error);
          })
      } else {
        alert("A schedule for this day does not exist!")
      }
      this.newDay = ''
      this.newStartTime = ''
      this.newEndTime = ''
    }
  }
}
