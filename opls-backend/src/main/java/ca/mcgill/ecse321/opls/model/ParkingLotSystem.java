package ca.mcgill.ecse321.opls.model;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Set;

import ca.mcgill.ecse321.opls.model.Schedule.Day;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * OPLS configuration model class. Contains parameters to be loaded in with the
 * application.
 */
@Entity
public class ParkingLotSystem extends NameIdModel {
	@Entity
	@Table(name = "parking_lot_system_schedule", uniqueConstraints = {
			@UniqueConstraint(columnNames = {"parking_lot_system_id", "day"})})
	public static class ParkingLotSystemSchedule extends Schedule {
		/**
		 * The schedule cannot navigate back to its ParkingLotSystem parent.
		 */
		@ManyToOne(optional = false)
		@JoinColumn(name = "parking_lot_system_id", nullable = false)
		private ParkingLotSystem parkingLotSystem;

		public void setParkingLotSystem(ParkingLotSystem parkingLotSystem) {
			this.parkingLotSystem = parkingLotSystem;
		}
	}

	/**
	 * Whether the configuration is active. Can only set to true (activate) or
	 * null (deactivate). The unique constraint only applies when the value is
	 * not null, so there can only be one non-null value, which gives our
	 * intended effect.
	 */
	@Column(nullable = true, unique = true)
	private Boolean isActive = null;

	/*
	 * Billing configuration
	 */

	// Fee for a monthly reservation.
	@Column(precision = 10, scale = 2)
	private BigDecimal monthlyFee;

	// Fee for an incremental reservation.
	@Column(precision = 10, scale = 2)
	private BigDecimal incrementFee;

	// Time increments in minutes for an incremental reservation.
	private int incrementTime;

	// Max time in minutes for an incremental reservation.
	private int maxIncrementTime;

	/**
	 * Each ParkingLotSystem can view its opening hours as a list of schedules.
	 */
	@OneToMany(mappedBy = "parkingLotSystem", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<ParkingLotSystemSchedule> schedules;

	public boolean isActive() {
		return isActive != null;
	}

	public void activate() {
		this.isActive = true;
	}

	public void deactivate() {
		this.isActive = null;
	}

	public double getMonthlyFee() {
		return monthlyFee.doubleValue();
	}

	public void setMonthlyFee(double monthlyFee) {
		this.monthlyFee = BigDecimal.valueOf(monthlyFee);
	}

	public double getIncrementFee() {
		return incrementFee.doubleValue();
	}

	public void setIncrementFee(double incrementFee) {
		this.incrementFee = BigDecimal.valueOf(incrementFee);
	}

	public int getIncrementTime() {
		return incrementTime;
	}

	public void setIncrementTime(int incrementTime) {
		this.incrementTime = incrementTime;
	}

	public int getMaxIncrementTime() {
		return maxIncrementTime;
	}

	public void setMaxIncrementTime(int maxIncrementTime) {
		this.maxIncrementTime = maxIncrementTime;
	}

	public Set<ParkingLotSystemSchedule> getSchedules() {
		return (Set<ParkingLotSystemSchedule>)schedules;
	}
	
	/**
	 * Add a schedule linked to the ParkingLotSystem instance.
	 * @param day Day of the schedule.
	 * @param startTime Start time.
	 * @param endTime End time.
	 */
	public ParkingLotSystemSchedule addSchedule(Day day, Time startTime,
			Time endTime) {
		ParkingLotSystemSchedule s = new ParkingLotSystemSchedule();
		s.setDay(day);
		s.setStartTime(startTime);
		s.setEndTime(endTime);
		s.setParkingLotSystem(this);
		return s;
	}
	
	/**
	 * Add a schedule linked to the ParkingLotSystem instance.
	 * @param day Day of the schedule.
	 * @param startTime Start time in format hh:mm:ss.
	 * @param endTime End time in format hh:mm:ss.
	 */
	public ParkingLotSystemSchedule addSchedule(Day day, String startTime, String endTime) {
		return addSchedule(day, Time.valueOf(startTime), Time.valueOf(endTime));
	}
}
