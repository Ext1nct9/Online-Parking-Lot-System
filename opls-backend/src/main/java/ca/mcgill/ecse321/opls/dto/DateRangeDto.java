package ca.mcgill.ecse321.opls.dto;

import java.util.Date;
import java.util.UUID;

import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Model for a date range.
 */
public class DateRangeDto {

	/** Optional ID. */
	public UUID uuid;

	/** Optional name. */
	public String name;

	/** Start date. */
	@NotNull
	public Date startDate;

	/** End date. */
	@NotNull
	public Date endDate;
	
	/** Duration in minutes. */
	@Min(0)
	public int durationMinutes;
	
	/** Default constructor. */
	public DateRangeDto() {}
	
	/** Constructor from model. */
	public DateRangeDto(DateRange dateRange) {
		this.uuid = dateRange.uuid;
		this.name = dateRange.name;
		this.startDate = dateRange.startDate;
		this.endDate = dateRange.endDate;
		this.durationMinutes = (int)dateRange.getDurationMinutes();
	}
	
}
