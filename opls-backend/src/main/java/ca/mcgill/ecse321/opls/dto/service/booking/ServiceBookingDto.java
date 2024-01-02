package ca.mcgill.ecse321.opls.dto.service.booking;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import ca.mcgill.ecse321.opls.DateHelper;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.VehicleService.VehicleServiceBooking;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Response model for a vehicle service booking.
 */
public class ServiceBookingDto {

	/**
	 * Booking uuid.
	 */
	@NotNull
	public UUID uuid;

	/**
	 * Booking status.
	 */
	@NotNull
	public BookingStatus status;

	/**
	 * Confirmation number for the booking.
	 */
	@NotNull
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
	@Positive
	public double cost;

	/**
	 * Requested Vehicle Service Id.
	 */
	@NotNull
	public String vehicleServiceId;

	/**
	 * License plate.
	 */
	@NotNull
	@Size(max = 15, message = "Invalid license plate.")
	public String licensePlate;

	/**
	 * The credit card number to pay for the booking.
	 */
	@NotNull
	@Size(min = 10, max = 16, message = "Invalid credit card number.")
	public String creditCardNumber;

	/** Default constructor. */
	public ServiceBookingDto() {
	}

	/** Constructor from database entity. */
	public ServiceBookingDto(VehicleServiceBooking vsb) {
		this.uuid = vsb.getUuid();
		this.status = vsb.getBookingStatus();
		this.licensePlate = vsb.getLicensePlate();
		this.startDate = vsb.getStartDate();
		this.endDate = vsb.getEndDate();
		this.confirmationNumber = vsb.getConfirmationNumber();
		this.cost = vsb.getCost();
		this.vehicleServiceId = vsb.getVehicleService().getId();
	}

}
