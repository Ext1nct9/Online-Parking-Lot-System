package ca.mcgill.ecse321.opls.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ca.mcgill.ecse321.opls.auth.OAuthHelper;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Booking model to be inherited.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Booking extends UuidModel {

	public static final int LICENSE_PLATE_LENGTH = 16;
	public static final int CONFIRM_NUM_LENGTH = 6;

	/** Wrapper class containing a date range. */
	public static class DateRange {
		/** Optional ID. */
		public UUID uuid;

		/** Optional name. */
		public String name;

		/** Start date. */
		public Date startDate;

		/** End date. */
		public Date endDate;

		public static DateRange rangeWithStartAndDuration(Date startDate,
				int duration, TimeUnit unit) {
			return new DateRange(startDate,
					new Date(startDate.getTime() + unit.toMillis(duration)));
		}

		/** Create a date range. */
		public DateRange(Date startDate, Date endDate) {
			this(null, null, startDate, endDate);
		}

		/** Create a date range. */
		public DateRange(UUID uuid, Date startDate, Date endDate) {
			this(uuid, null, startDate, endDate);
		}

		/** Create a date range. */
		public DateRange(UUID uuid, String name, Date startDate, Date endDate) {
			this.uuid = uuid;
			this.name = name;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		/** Get the duration in minutes of the date range. */
		public long getDurationMs() {
			return endDate.getTime() - startDate.getTime();
		}

		/** Get the duration in minutes of the date range. */
		public long getDurationMinutes() {
			return (endDate.getTime() - startDate.getTime()) / (60 * 1000);
		}
	}

	/** Status of a booking. */
	public enum BookingStatus {
		@JsonProperty("requested")
		REQUESTED,

		@JsonProperty("paid")
		PAID,

		@JsonProperty("confirmed")
		CONFIRMED,

		@JsonProperty("completed")
		COMPLETED
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private BookingStatus bookingStatus = BookingStatus.REQUESTED;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date endDate;

	/**
	 * Any concrete instance of Booking can navigate to its customer. The
	 * Booking may not have a registered customer, so it is optional.
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "customer_id", nullable = true)
	private Customer customer;

	@Column(length = CONFIRM_NUM_LENGTH)
	private String confirmationNumber;

	@Column(nullable = false, length = LICENSE_PLATE_LENGTH)
	private String licensePlate;

	@Column(nullable = true, precision = 10, scale = 2)
	private BigDecimal cost = BigDecimal.ZERO;

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getConfirmationNumber() {
		return confirmationNumber;
	}

	public void generateConfirmationNumber() {
		this.confirmationNumber = OAuthHelper.randomString(CONFIRM_NUM_LENGTH)
				.toUpperCase();
	}

	public void setConfirmationNumber(String confirmationNumber) {
		this.confirmationNumber = confirmationNumber;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public double getCost() {
		return cost.doubleValue();
	}

	public void setCost(double cost) {
		this.cost = BigDecimal.valueOf(cost);
	}

	public DateRange getDateRange() {
		return new DateRange(this.startDate, this.endDate);
	}

	public void setDateRange(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void setDateRange(DateRange dateRange) {
		this.startDate = dateRange.startDate;
		this.endDate = dateRange.endDate;
	}

	/**
	 * Set the date range and duration of the booking.
	 * 
	 * @param startDate
	 *            Start of the booking.
	 * @param minuteDuration
	 *            Duration of the booking in minutes.
	 */
	public void setDateRangeMinutes(Date startDate, int minuteDuration) {
		this.setDateRange(DateRange.rangeWithStartAndDuration(startDate,
				minuteDuration, TimeUnit.MINUTES));
	}

}
