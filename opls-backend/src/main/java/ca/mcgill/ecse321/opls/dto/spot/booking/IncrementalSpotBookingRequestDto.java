package ca.mcgill.ecse321.opls.dto.spot.booking;

import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request for an incremental parking spot booking.
 */
public class IncrementalSpotBookingRequestDto {

	/**
	 * The parking spot for the booking.
	 */
	@NotNull
	@Size(min = 4, max = 4, message = "Invalid parking spot ID.")
	public String parkingSpotId;

	/**
	 * The credit card number to pay for the booking.
	 */
	@NotNull
	@Size(min = 10, max = 16, message = "Invalid credit card number.")
	public String creditCardNumber;

	/**
	 * Length of the booking in minutes.
	 */
	@Min(value = 1, message = "Duration must be positive.")
	public int duration;

	/**
	 * License plate.
	 */
	@NotNull
	@Size(max = 15, message = "Invalid license plate.")
	public String licensePlate;

	/**
	 * Requested vehicle type. Must match with the parking spot.
	 */
	@NotNull
	public VehicleType vehicleType;

}
