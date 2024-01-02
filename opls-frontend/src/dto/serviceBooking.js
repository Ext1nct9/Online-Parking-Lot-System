
import { getDate, toDateString } from "./../utils/dateHelper";

/** Status of a booking. */
export const BookingStatus = {
  requested: "requested",
  paid: "paid",
  confirmed: "confirmed",
  completed: "completed"
}
/** Request to create a booking. */
export const ServiceBookingRequestDto = function(licensePlate, creditCardNumber, startDate){
  startDate = startDate || getDate();
  return{
    licensePlate: licensePlate,
    creditCardNumber: creditCardNumber,
    startDate: toDateString(startDate)
  }
}

/** Request to search for bookings. */
export const ServiceBookingQueryRequestDto = function(queryOwn, serviceIds, startDate, endDate) {
  startDate = startDate || getDate();
  endDate = endDate || new Date(startDate.getTime() + 30 * 60 * 1000);
  return {
    queryOwn: queryOwn || false,
    vehicleServiceIds: serviceIds || [],
    startDate: toDateString(startDate),
    endDate: toDateString(endDate)
  }
}

/** Request to update a booking. */
export const UpdateServiceBookingRequestDto = function(licensePlate, bookingStatus) {
  return {
    licensePlate: licensePlate,
    bookingStatus: bookingStatus
  }
}
