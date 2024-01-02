package ca.mcgill.ecse321.opls.dto.service.booking;

import java.util.Collection;

import ca.mcgill.ecse321.opls.dto.DateRangeDto;
import jakarta.validation.constraints.Min;

/**
 * Response model for querying bookings.
 */
public class ServiceBookingQueryResponseDto {

	/**
	 * The queried bookings.
	 */
	public Collection<DateRangeDto> bookings = null;

	/**
	 * The number of queried bookings.
	 */
	@Min(0)
	public int count;

	/** Default constructor. */
	public ServiceBookingQueryResponseDto() {
	}

	/** Constructor with fields. */
	public ServiceBookingQueryResponseDto(Collection<DateRangeDto> bookings) {
		this.bookings = bookings;
		this.count = bookings == null ? 0 : bookings.size();
	}

}
