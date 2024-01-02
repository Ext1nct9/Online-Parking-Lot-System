package ca.mcgill.ecse321.opls.dto.spot.booking;

import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;

/**
 * Response for a parking spot booking request.
 */
public class PatchSpotBookingRequestDto {

	/**
	 * Booking status.
	 */
	public BookingStatus status;

	/**
	 * Parking spot for the booking, null if not yet assigned.
	 */
	public String parkingSpotId;

}
