package ca.mcgill.ecse321.opls.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.model.VehicleService.VehicleServiceBooking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Repository to query the vehicle_service_booking table for entries in a
 * specific date range.
 */
interface VehicleServiceBookingByRangeRepository {

	/**
	 * Query vehicle service bookings.
	 * 
	 * @param customerId
	 *            Id of the booking customer, null if do not apply condition.
	 * @param vehicleServiceId
	 *            Ids of the vehicle services to query, empty or null if do not
	 *            apply condition.
	 * @param startDate
	 *            Start of the query range.
	 * @param endDate
	 *            End of the query range.
	 * @return Queried results.
	 */
	Iterable<DateRange> query(Integer customerId,
			Collection<String> vehicleServiceIds, Date startDate, Date endDate);

	/**
	 * Query vehicle service bookings.
	 * 
	 * @param vehicleServiceIds
	 *            Ids of the vehicle services to query, empty or null if do not
	 *            apply condition.
	 * @param startDate
	 *            Start of the query range.
	 * @param endDate
	 *            End of the query range.
	 * @return Queried results.
	 */
	default Iterable<DateRange> query(Collection<String> vehicleServiceIds,
			Date startDate, Date endDate) {
		return query(null, vehicleServiceIds, startDate, endDate);
	}

	/**
	 * Query vehicle service bookings.
	 * 
	 * @param vehicleServices
	 *            The vehicle services to query, empty or null if do not apply
	 *            condition.
	 * @param startDate
	 *            Start of the query range.
	 * @param endDate
	 *            End of the query range.
	 * @return Queried results.
	 */
	default Iterable<DateRange> query(VehicleService[] vehicleServices,
			Date startDate, Date endDate) {
		Collection<String> ids = null;
		if (vehicleServices != null && vehicleServices.length > 0) {
			ids = new ArrayList<String>();
			for (var service : vehicleServices) {
				ids.add(service.getId());
			}
		}

		return query(null, ids, startDate, endDate);
	}

	/**
	 * Query vehicle service bookings.
	 * 
	 * @param vehicleService
	 *            Vehicle service to query, null if do not apply condition.
	 * @param startDate
	 *            Start of the query range.
	 * @param endDate
	 *            End of the query range.
	 * @return Queried results.
	 */
	default Iterable<DateRange> query(VehicleService vehicleService,
			Date startDate, Date endDate) {
		Collection<String> ids = null;
		if (vehicleService != null) {
			ids = Collections.singleton(vehicleService.getId());
		}

		return query(null, ids, startDate, endDate);
	}

}

/** Implementation of the query by date range using EntityManager. */
class VehicleServiceBookingByRangeRepositoryImpl
		implements
			VehicleServiceBookingByRangeRepository {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Query vehicle service bookings.
	 * 
	 * @param customerId
	 *            Id of the booking customer, null if do not apply condition.
	 * @param vehicleServiceIds
	 *            Ids of the vehicle services to query, null or empty if do not
	 *            apply condition.
	 * @param startDate
	 *            Start of the query range.
	 * @param endDate
	 *            End of the query range.
	 * @return Queried results.
	 */
	@Override
	public Iterable<DateRange> query(Integer customerId,
			Collection<String> vehicleServiceIds, Date startDate,
			Date endDate) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DateRange> query = cb.createQuery(DateRange.class);
		Root<VehicleServiceBooking> vsb = query
				.from(VehicleServiceBooking.class);

		// range parameters
		var startRange = cb.parameter(Date.class, "startRange");
		var endRange = cb.parameter(Date.class, "endRange");

		// create predicate list
		var predicates = new ArrayList<Predicate>();

		// query within date range
		/*
		 * ((:startRange >= e.startDate AND :startRange <= e.endDate) OR " +
		 * "(e.startDate >= :startRange AND e.startDate <= :endRange)
		 */
		var idPath = vsb.<UUID>get("uuid");
		var startDatePath = vsb.<Date>get("startDate");
		var endDatePath = vsb.<Date>get("endDate");
		predicates.add(cb.or(
				cb.and(cb.greaterThanOrEqualTo(startRange, startDatePath),
						cb.lessThanOrEqualTo(startRange, endDatePath)),
				cb.and(cb.greaterThanOrEqualTo(startDatePath, startRange),
						cb.lessThanOrEqualTo(startDatePath, endRange))));
		
		// query confirmed bookings
		var statusPath = vsb.<BookingStatus>get("bookingStatus");
		predicates.add(cb.equal(statusPath, BookingStatus.CONFIRMED));

		// query by customer
		if (customerId != null) {
			var customerJoinPath = vsb.<Customer>get("customer")
					.<Integer>get("id");
			predicates.add(cb.equal(customerJoinPath, customerId));
		}

		// query by vehicle services
		if (vehicleServiceIds != null && vehicleServiceIds.size() > 0) {
			var serviceJoinPath = vsb.<VehicleService>get("vehicleService")
					.<String>get("id");

			var servicePredicates = new Predicate[vehicleServiceIds.size()];
			int i = 0;
			for (var serviceId : vehicleServiceIds) {
				servicePredicates[i++] = cb.equal(serviceJoinPath, serviceId);
			}
			predicates.add(cb.or(servicePredicates));
		}

		// combine predicates
		var predicate = cb.and(predicates.toArray(new Predicate[]{}));
		var serviceNamePath = vsb.<VehicleService>get("vehicleService")
				.<String>get("displayName");
		query.multiselect(idPath, serviceNamePath, startDatePath,
				endDatePath).where(predicate);

		// execute
		return entityManager.createQuery(query)
				.setParameter("startRange", startDate)
				.setParameter("endRange", endDate).getResultList();
	}

}

/**
 * Repository to interface with the vehicle_service_booking table.
 */
public interface VehicleServiceBookingRepository
		extends
			CrudRepository<VehicleServiceBooking, Integer>,
			VehicleServiceBookingByRangeRepository {

	/** Find all bookings for a customer. */
	Iterable<VehicleServiceBooking> findVehicleServiceBookingsByCustomer(
			Customer customer);

	/** Find a booking by its uuid. */
	VehicleServiceBooking findVehicleServiceBookingByUuid(UUID uuid);

	/** Find a booking by its id. */
	VehicleServiceBooking findVehicleServiceBookingById(int id);

	/** Find a booking by its confirmation number. */
	VehicleServiceBooking findVehicleServiceBookingByConfirmationNumber(
			String confirmationNumber);

}
