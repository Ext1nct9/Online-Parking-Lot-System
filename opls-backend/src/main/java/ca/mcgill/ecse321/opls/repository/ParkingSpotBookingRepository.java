package ca.mcgill.ecse321.opls.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotBooking;

/**
 * Saved queries for the parking_spot_booking table.
 */
class ParkingSpotBookingQueryHelper {

	/** Fetch whether the parking spot has active bookings. */
	public static final String IS_CURRENT_ACTIVE_QUERY = "SELECT EXISTS(SELECT 1 FROM parking_spot_booking WHERE "
			+ "parking_spot_id = :parkingSpotId AND "
			+ QueryHelper.WhereClause.CURRENT_ACTIVE + ")";

	/** Fetch the current booking for the parking spot. */
	public static final String CURRENT_ACTIVE_QUERY = "SELECT * FROM parking_spot_booking WHERE "
			+ "parking_spot_id = :parkingSpotId AND "
			+ QueryHelper.WhereClause.CURRENT_ACTIVE;
	
	/** Fetch a customer's active bookings. */
	public static final String CUSTOMER_ACTIVE_QUERY = "SELECT * FROM parking_spot_booking WHERE "
			+ "customer_id = :customerId AND "
			+ QueryHelper.WhereClause.CURRENT_ACTIVE;

}

/**
 * Repository to interface with the parking_spot_booking table.
 */
public interface ParkingSpotBookingRepository
		extends
			CrudRepository<ParkingSpotBooking, Integer> {

	/** Find all parking spot bookings for a customer. */
	Iterable<ParkingSpotBooking> findParkingSpotBookingsByCustomer(
			Customer customer);

	/** Find a parking spot booking by its uuid value. */
	ParkingSpotBooking findParkingSpotBookingByUuid(UUID uuid);

	/** Find a parking spot booking by its confirmation number. */
	ParkingSpotBooking findParkingSpotBookingByConfirmationNumber(
			String confirmationNumber);

	/** Find parking spot bookings by status. */
	Iterable<ParkingSpotBooking> findParkingSpotBookingsByBookingStatus(
			BookingStatus status);

	/** Determine if an active booking exists on the parking spot. */
	@Query(value = ParkingSpotBookingQueryHelper.IS_CURRENT_ACTIVE_QUERY, nativeQuery = true)
	boolean isParkingSpotBooked(@Param("parkingSpotId") String parkingSpotId);

	/** Determine if an active booking exists on the parking spot. */
	default boolean isParkingSpotBooked(ParkingSpot parkingSpot) {
		return isParkingSpotBooked(parkingSpot.getId());
	}

	/** Fetch the current booking for the parking spot. */
	@Query(value = ParkingSpotBookingQueryHelper.CURRENT_ACTIVE_QUERY, nativeQuery = true)
	ParkingSpotBooking getActiveBooking(
			@Param("parkingSpotId") String parkingSpotId);

	/** Fetch the current booking for the parking spot. */
	default ParkingSpotBooking getActiveBooking(ParkingSpot parkingSpot) {
		return getActiveBooking(parkingSpot.getId());
	}
	
	/** Fetch active bookings for a customer. */
	@Query(value = ParkingSpotBookingQueryHelper.CUSTOMER_ACTIVE_QUERY, nativeQuery = true)
	Iterable<ParkingSpotBooking> getCustomerActiveBookings(@Param("customerId") int customerId);
	
	/** Fetch active bookings for a customer. */
	default Iterable<ParkingSpotBooking> getCustomerActiveBookings(Customer customer) {
		return getCustomerActiveBookings(customer.getId());
	}

}
