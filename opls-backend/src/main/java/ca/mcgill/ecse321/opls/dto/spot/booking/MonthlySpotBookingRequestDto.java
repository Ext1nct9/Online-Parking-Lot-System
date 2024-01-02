package ca.mcgill.ecse321.opls.dto.spot.booking;

import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request for a monthly parking spot booking.
 */
public class MonthlySpotBookingRequestDto {

	/**
	 * The credit card number to pay for the booking.
	 */
	@NotNull
	@Size(min = 16, max = 16, message = "Invalid credit card number.")
	public String creditCardNumber;

	/**
	 * License plate.
	 */
	@NotNull
	@Size(max = 15, message = "Invalid license plate.")
	public String licensePlate;

	/**
	 * Requested vehicle type. Must be VehicleType.REGULAR.
	 */
	@NotNull
	public VehicleType vehicleType;

}
