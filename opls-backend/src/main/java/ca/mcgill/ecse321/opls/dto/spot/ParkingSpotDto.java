package ca.mcgill.ecse321.opls.dto.spot;

import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Response model for a parking spot.
 */
public class ParkingSpotDto {

	/**
	 * The parking spot id.
	 */
	@NotEmpty
	@Size(min = 4, max = 4, message = "Invalid spot ID length.")
	public String id;

	/**
	 * The vehicle type.
	 */
	@NotNull
	public VehicleType vehicleType = VehicleType.REGULAR;

	/**
	 * The parking spot status.
	 */
	@NotNull
	public ParkingSpotStatus parkingSpotStatus = ParkingSpotStatus.OPEN;

	/**
	 * The message.
	 */
	public String message;

	/** Default constructor. */
	public ParkingSpotDto() {
	}

	/** Constructor from the internal model. */
	public ParkingSpotDto(ParkingSpot parkingSpot) {
		this.id = parkingSpot.getId();
		this.vehicleType = parkingSpot.getVehicleType();
		this.parkingSpotStatus = parkingSpot.getParkingSpotStatus();
		this.message = parkingSpot.getMessage();
	}

	/** Convert the DTO into a database entity. */
	public ParkingSpot toModel() {
		ParkingSpot spot = new ParkingSpot();

		spot.overrideId(id);
		spot.setMessage(message);
		spot.setParkingSpotStatus(parkingSpotStatus);
		spot.setVehicleType(vehicleType);

		return spot;
	}

}
