
/** Parking spot statuses. */
export const ParkingSpotStatus = {
  open: "open",
  reserved: "reserved",
  closed: "closed",
};

/** Parking spot sizes. */
export const VehicleType = {
  regular: "regular",
  large: "large"
};

/** Create a parking spot query request. */
export const ParkingSpotQueryRequestDto = (unbooked = false, floors = [], statuses = [], vehicleTypes = []) => {
  return {
    unbooked: unbooked,
    floors: floors,
    statuses: statuses,
    vehicleTypes: vehicleTypes,
  };
};
