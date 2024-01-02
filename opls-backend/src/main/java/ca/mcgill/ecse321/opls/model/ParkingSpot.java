package ca.mcgill.ecse321.opls.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Model for a parking spot in the parking lot.
 */
@Entity
public class ParkingSpot {
	
	/**
	 * Model for a booking for a parking spot.
	 */
	@Entity
	@Table(name = "parking_spot_booking")
	public static class ParkingSpotBooking extends Booking {
		/**
		 * A ParkingSpotBooking can see its associated ParkingSpot.
		 */
		@ManyToOne(optional = true, fetch = FetchType.EAGER)
		@JoinColumn(name = "parking_spot_id", nullable = true)
		private ParkingSpot parkingSpot;

		// Not null if the reservation should be automatically renewed at the
		// end.
		private String billingAccountId;

		public ParkingSpot getParkingSpot() {
			return parkingSpot;
		}

		public void setParkingSpot(ParkingSpot parkingSpot) {
			this.parkingSpot = parkingSpot;
		}

		public String getBillingAccountId() {
			return billingAccountId;
		}

		public void setBillingAccountId(String billingAccountId) {
			this.billingAccountId = billingAccountId;
		}
	}

	/**
	 * Types of vehicles that can park in a parking spot.
	 */
	public enum VehicleType {
		@JsonProperty("regular")
		REGULAR,

		@JsonProperty("large")
		LARGE
	}

	/**
	 * Parking spot statuses.
	 */
	public enum ParkingSpotStatus {
		@JsonProperty("open")
		OPEN,

		@JsonProperty("reserved")
		RESERVED,
		
		@JsonProperty("closed")
		CLOSED
	}

	@Id
	@Column(nullable = false, length = 10)
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private VehicleType vehicleType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private ParkingSpotStatus parkingSpotStatus;

	private String message;

	/**
	 * A ParkingSpot can see its current booking and history of bookings.
	 */
	@OneToMany(mappedBy = "parkingSpot", cascade = CascadeType.ALL)
	private Set<ParkingSpotBooking> bookings;

	/** Default constructor */
	public ParkingSpot() {
	}

	/**
	 * Create a new parking spot.
	 * 
	 * @param floor
	 *            The floor identifier (maximum 1 character).
	 * @param num
	 *            The spot number (maximum 3 digits).
	 * @param vehicleType
	 *            The vehicle size for the parking spot.
	 * @param parkingSpotStatus
	 *            The status of the parking spot.
	 */
	public ParkingSpot(char floor, int num, VehicleType vehicleType,
			ParkingSpotStatus parkingSpotStatus) {
		if (floor <= ' ') {
			this.id = String.format("%03d", num);
		} else {
			this.id = String.format("%c%03d", floor, num);
		}

		this.vehicleType = vehicleType;
		this.parkingSpotStatus = parkingSpotStatus;
	}

	/**
	 * Create a new open parking spot.
	 * 
	 * @param floor
	 *            The floor identifier.
	 * @param num
	 *            The spot number (maximum 3 digits).
	 * @param vehicleType
	 *            The vehicle size for the parking spot.
	 */
	public ParkingSpot(char floor, int num, VehicleType vehicleType) {
		this(floor, num, vehicleType, ParkingSpotStatus.OPEN);
	}

	/**
	 * Create a new regular sized parking spot.
	 * 
	 * @param floor
	 *            The floor identifier.
	 * @param num
	 *            The spot number (maximum 3 digits).
	 * @param parkingSpotStatus
	 *            The status of the parking spot.
	 */
	public ParkingSpot(char floor, int num,
			ParkingSpotStatus parkingSpotStatus) {
		this(floor, num, VehicleType.REGULAR, parkingSpotStatus);
	}

	/**
	 * Create a new regular sized and open parking spot.
	 * 
	 * @param floor
	 *            The floor identifier.
	 * @param num
	 *            The spot number (maximum 3 digits).
	 */
	public ParkingSpot(char floor, int num) {
		this(floor, num, VehicleType.REGULAR, ParkingSpotStatus.OPEN);
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	public ParkingSpotStatus getParkingSpotStatus() {
		return parkingSpotStatus;
	}

	public void setParkingSpotStatus(ParkingSpotStatus parkingSpotStatus) {
		this.parkingSpotStatus = parkingSpotStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void overrideId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ParkingSpotBooking newBooking() {
		ParkingSpotBooking psb = new ParkingSpotBooking();
		psb.setParkingSpot(this);
		return psb;
	}
	
}
