export const claims = {
  admin: "ADMIN",
  employee: "EMPLOYEE",
  customer: "CUSTOMER",
};

/** List of functions to generate routes. */
export const routes = {
  HELLO: () => `/`,
  LOGIN: () => `/login`,
  CREATE_ACCOUNT: () => `/account`, 

  ADMIN_DASHBOARD: () => `/dashboard/admin`,
  CUSTOMER_DASHBOARD: () => `/dashboard/customer`,
  EMPLOYEE_DASHBOARD: () => `/dashboard/employee`,

  ACCOUNT_INFORMATION: () => `/dashboard/account`,

  NEW_SPOT_BOOKING: () => `/spot/booking`,
  SPOT_BOOKING_LIST: () => `/spot/booking/list`,

  NEW_PARKING_SPOT: () => `/spot`,
  PARKING_SPOT_LIST: () => `/spot/list`,
  PARKING_SPOT_INFORMATION: (id) => `/spot/${id || ":id"}`,

  NEW_EMPLOYEE: () => `/employee`,
  EMPLOYEE_LIST: () => `/employee/list`,
  EMPLOYEE: (uuid) => `/employee/${uuid || ":uuid"}`,
  
  NEW_SERVICE_BOOKING: (serviceId) => `/service/${serviceId || ":serviceId"}/booking`,
  SERVICE_BOOKING_LIST: () => `/service/booking/search`,

  NEW_SERVICE: () => `/service`,
  SERVICE_LIST: () => `/service/list`,
  SERVICE: (id) => `/service/${id || ":id"}`,
};

/** List of route objects. */
export const routesList = [
  {
    path: routes.HELLO(),
    name: 'Hello',
    component: () => import("@/components/Hello"),
    meta: {
      loggedIn: false,
      userClaims: [],
    },
  },
  {
    path: routes.LOGIN(),
    name: "Login",
    component: () => import("@/components/Login"),
    meta: {
      loggedIn: false,
      userClaims: [],
    },
  },
  {
    path: routes.CREATE_ACCOUNT(),
    name: "Create Account",
    component: () => import("@/components/CreateAccount"),
    meta: {
      loggedIn: false,
      userClaims: [],
    },
  },
  {
    path: routes.ADMIN_DASHBOARD(),
    name: "Administrator Dashboard",
    component: () => import("@/components/AdminDashboard"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
  {
    path: routes.CUSTOMER_DASHBOARD(),
    name: "Customer Dashboard",
    component: () => import("@/components/CustomerDashboard"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.customer ],
    },
  },
  {
    path: routes.EMPLOYEE_DASHBOARD(),
    name: "Employee Dashboard",
    component: () => import("@/components/EmployeeDashboard"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.employee ],
    },
  },
  {
    path: routes.ACCOUNT_INFORMATION(),
    name: "Account Information",
    component: () => import("@/components/Account"),
    meta: {
      loggedIn: true,
      userClaims: [],
    },
  },

  {
    path: routes.NEW_SPOT_BOOKING(),
    name: "Parking Spot Booking",
    component: () => import("@/components/NewSpotBooking"),
    meta: {
      loggedIn: false,
      userClaims: [],
    },
  },
  {
    path: routes.SPOT_BOOKING_LIST(),
    name: "Parking Spot Bookings",
    component: () => import("@/components/SpotBookingList"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin, claims.employee ],
    },
  },

  {
    path: routes.PARKING_SPOT_LIST(),
    name: "Parking Spots",
    component: () => import("@/components/SpotList"),
    meta: {
      loggedIn: false,
      userClaims: [],
    },
  },
  {
    path: routes.NEW_PARKING_SPOT(),
    name: "New Parking Spot",
    component: () => import("@/components/NewSpot"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
  {
    path: routes.PARKING_SPOT_INFORMATION(),
    name: "Parking Spot Information",
    component: () => import("@/components/Spot"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin, claims.employee ],
    },
  },

  {
    path: routes.NEW_EMPLOYEE(),
    name: "New Employee",
    component: () => import("@/components/NewEmployee"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
  {
    path: routes.EMPLOYEE_LIST(),
    name: "Employee List",
    component: () => import("@/components/EmployeeList"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
  {
    path: routes.EMPLOYEE(),
    name: "Employee",
    component: () => import("@/components/Employee"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },

  {
    path: routes.NEW_SERVICE_BOOKING(),
    name: "New Service Booking",
    component: () => import("@/components/NewServiceBooking"),
    meta: {
      loggedIn: false,
      userClaims: [],
    },
  },
  {
    path: routes.SERVICE_BOOKING_LIST(),
    name: "Vehicle Service Booking List",
    component: () => import("@/components/ServiceBookingList"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin, claims.employee ],
    },
  },

  {
    path: routes.NEW_SERVICE(),
    name: "New Service",
    component: () => import("@/components/NewService"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
  {
    path: routes.SERVICE_LIST(),
    name: "Vehicle Service List",
    component: () => import("@/components/ServiceList"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
  {
    path: routes.SERVICE(),
    name: "Vehicle Service",
    component: () => import("@/components/Service"),
    meta: {
      loggedIn: true,
      userClaims: [ claims.admin ],
    },
  },
];
