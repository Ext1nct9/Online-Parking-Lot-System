package ca.mcgill.ecse321.opls.dto.spot;

import java.util.Collection;

import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;

/**
 * Search request for parking spots. Contains all possible queryable fields.
 */
public class ParkingSpotQueryRequestDto {

	/**
	 * Whether to only search for parking spots without bookings.
	 */
	public boolean unbooked = true;

	/**
	 * The set of floors to search on. Defaults to all.
	 */
	public Collection<Character> floors = null;

	/**
	 * The set of statuses to search for. Defaults to all.
	 */
	public Collection<ParkingSpotStatus> statuses = null;

	/**
	 * The set of vehicle types to search for. Defaults to all.
	 */
	public Collection<VehicleType> vehicleTypes = null;

}
