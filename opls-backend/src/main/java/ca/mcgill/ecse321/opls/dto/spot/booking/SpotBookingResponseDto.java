package ca.mcgill.ecse321.opls.dto.spot.booking;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import ca.mcgill.ecse321.opls.DateHelper;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotBooking;

/**
 * Response for a parking spot booking response.
 */
public class SpotBookingResponseDto {

	/**
	 * ID of the booking.
	 */
	public UUID uuid;

	/**
	 * Booking status.
	 */
	public BookingStatus status;

	/**
	 * Parking spot for the booking, null if not yet assigned.
	 */
	public String parkingSpotId;

	/**
	 * Confirmation number for the booking.
	 */
	public String confirmationNumber;

	/**
	 * Start date for the reservation.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.DATE_FORMAT)
	public Date startDate;

	/**
	 * End date for the reservation.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.DATE_FORMAT)
	public Date endDate;

	/**
	 * Amount charged to the payment method.
	 */
	public double cost;

	/** Default constructor. */
	public SpotBookingResponseDto() {
	}

	/** Construct from model class. */
	public SpotBookingResponseDto(ParkingSpotBooking booking) {
		this.uuid = booking.getUuid();
		this.cost = booking.getCost();
		if (booking.getParkingSpot() != null) {
			this.parkingSpotId = booking.getParkingSpot().getId();
		}
		this.startDate = booking.getStartDate();
		this.endDate = booking.getEndDate();
		this.status = booking.getBookingStatus();
		this.confirmationNumber = booking.getConfirmationNumber();
	}

}
