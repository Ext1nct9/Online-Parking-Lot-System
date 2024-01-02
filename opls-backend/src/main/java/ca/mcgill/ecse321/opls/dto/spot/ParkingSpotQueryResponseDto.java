package ca.mcgill.ecse321.opls.dto.spot;

import java.util.Collection;

import jakarta.validation.constraints.Min;

/**
 * Result for a parking spot search.
 */
public class ParkingSpotQueryResponseDto {

	/**
	 * The parking spots.
	 */
	public Collection<ParkingSpotDto> parkingSpots;

	/**
	 * The number of returned results.
	 */
	@Min(0)
	public int count;

	/** Default constructor. */
	public ParkingSpotQueryResponseDto() {
	}
	
	/** Constructor with fields. */
	public ParkingSpotQueryResponseDto(Collection<ParkingSpotDto> parkingSpots) {
		this.parkingSpots = parkingSpots;
		this.count = parkingSpots == null ? 0 : parkingSpots.size();
	}

}
