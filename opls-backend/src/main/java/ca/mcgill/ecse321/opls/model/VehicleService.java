package ca.mcgill.ecse321.opls.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Model for a service the parking lot offers.
 */
@Entity
public class VehicleService extends NameIdModel {
	/**
	 * Model for a booking for a vehicle service.
	 */
	@Entity
	@Table(name = "vehicle_service_booking")
	public static class VehicleServiceBooking extends Booking {
		/**
		 * A VehicleServiceBooking can see its associated VehicleService.
		 */
		@ManyToOne(optional = false, fetch = FetchType.EAGER)
		@JoinColumn(name = "vehicle_service_id", nullable = false)
		private VehicleService vehicleService;

		public VehicleService getVehicleService() {
			return vehicleService;
		}

		public void setVehicleService(VehicleService vehicleService) {
			this.vehicleService = vehicleService;
		}
	}

	/** Duration in minutes. */
	@Column(nullable = false)
	private int duration;

	@Column(precision = 10, scale = 2, nullable = false)
	private BigDecimal fee = BigDecimal.ZERO;

	public int getDuration() {
		return duration;
	}

	public void setDuration(int minutes) {
		this.duration = minutes;
	}

	public double getFee() {
		return fee.doubleValue();
	}

	public void setFee(double fee) {
		this.fee = BigDecimal.valueOf(fee);
	}

	public VehicleServiceBooking newBooking(Date startDate) {
		VehicleServiceBooking vsb = new VehicleServiceBooking();
		vsb.setVehicleService(this);
		vsb.setCost(this.getFee());
		vsb.setStartDate(startDate);
		vsb.setDateRangeMinutes(startDate, duration);
		return vsb;
	}

}