package ca.mcgill.ecse321.opls.dto.service.booking;

import java.util.Collection;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import ca.mcgill.ecse321.opls.DateHelper;
import jakarta.validation.constraints.NotNull;

/**
 * Request model for querying vehicle service bookings.
 */
public class ServiceBookingQueryRequestDto {

	/**
	 * Whether to only query the user's own bookings.
	 */
	public boolean queryOwn = false;

	/**
	 * The services to query for.
	 */
	public Collection<String> vehicleServiceIds = null;

	/**
	 * The start date of the query range.
	 */
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.DATE_FORMAT)
	public Date startDate;

	/**
	 * The end date of the query range.
	 */
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.DATE_FORMAT)
	public Date endDate;

}
