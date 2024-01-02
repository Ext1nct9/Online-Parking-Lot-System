package ca.mcgill.ecse321.opls.dto.service.booking;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import ca.mcgill.ecse321.opls.DateHelper;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request model for a vehicle service booking.
 */
public class ServiceBookingRequestDto {

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
	 * The start date.
	 */
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.DATE_FORMAT)
	public Date startDate;

	/** Default constructor. */
	public ServiceBookingRequestDto() {
	}

	/** Constructor with fields. */
	public ServiceBookingRequestDto(String licensePlate,
			String creditCardNumber, Date startDate) {
		this.licensePlate = licensePlate;
		this.creditCardNumber = creditCardNumber;
		this.startDate = startDate;
	}
}
