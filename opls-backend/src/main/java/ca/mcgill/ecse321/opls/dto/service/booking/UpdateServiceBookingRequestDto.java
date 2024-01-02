package ca.mcgill.ecse321.opls.dto.service.booking;

import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request model to update a vehicle service booking.
 */
public class UpdateServiceBookingRequestDto {

	/**
	 * License plate.
	 */
	@Size(max = 15, message = "Invalid license plate.")
	public String licensePlate;

	/**
	 * New status of the booking.
	 */
	public BookingStatus bookingStatus = null;

}
