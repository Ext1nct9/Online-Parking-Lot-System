package ca.mcgill.ecse321.opls.service;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.model.VehicleService.VehicleServiceBooking;
import ca.mcgill.ecse321.opls.repository.CustomerRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemScheduleRepository;
import ca.mcgill.ecse321.opls.repository.VehicleServiceBookingRepository;
import ca.mcgill.ecse321.opls.repository.VehicleServiceRepository;

@Service
public class VehicleServiceBookingService {

	@Autowired
	private VehicleServiceBookingRepository bookingRepository;

	@Autowired
	private VehicleServiceRepository serviceRepository;

	@Autowired
	private ParkingLotSystemScheduleRepository scheduleRepository;

	@Autowired
	private CustomerRepository customerRepository;

	/**
	 * Get vehicle service booking by id.
	 * 
	 * @param id
	 *            Id of the booking.
	 * @return the booking, if found.
	 */
	@Transactional
	public VehicleServiceBooking getVehicleServiceBooking(int id) {
		var booking = bookingRepository.findVehicleServiceBookingById(id);
		if (booking == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Booking not found.");
		}
		return booking;
	}

	/**
	 * Get booking by uuid.
	 * 
	 * @param uuid
	 *            UUID of the booking.
	 * @return the booking, if found.
	 */
	@Transactional
	public VehicleServiceBooking getVehicleServiceBooking(UUID uuid) {
		var booking = bookingRepository.findVehicleServiceBookingByUuid(uuid);
		if (booking == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Booking not found.");
		}
		return booking;
	}

	/**
	 * Get booking by confirmation number.
	 * 
	 * @param confirmationNumber
	 *            confirmation number of the booking.
	 * @return the booking, if found.
	 */
	@Transactional
	public VehicleServiceBooking getVehicleServiceBooking(
			String confirmationNumber) {
		var booking = bookingRepository
				.findVehicleServiceBookingByConfirmationNumber(
						confirmationNumber);
		if (booking == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Booking not found.");
		}
		return booking;
	}

	/**
	 * Update a vehicle service booking.
	 * 
	 * @param uuid
	 *            UUID of the booking.
	 * @param licensePlate
	 *            License plate of the car for the booking.
	 * @return the updated booking.
	 */
	@Transactional
	public VehicleServiceBooking updateVehicleServiceBooking(UUID uuid,
			String licensePlate, BookingStatus bookingStatus) {
		var booking = getVehicleServiceBooking(uuid);
		if (licensePlate != null) {
			booking.setLicensePlate(licensePlate);
		}
		if (bookingStatus != null) {
			booking.setBookingStatus(bookingStatus);
		}
		return bookingRepository.save(booking);
	}

	/**
	 * Delete a vehicle service booking.
	 * 
	 * @param id
	 *            Id of the booking.
	 * @return The deleted booking.
	 */
	@Transactional
	public VehicleServiceBooking deleteVehicleServiceBooking(UUID id) {
		var booking = getVehicleServiceBooking(id);
		if (booking == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Booking not found.");
		}
		bookingRepository.delete(booking);
		return booking;
	}

	/**
	 * Validate a vehicle service booking request.
	 * 
	 * @param id
	 *            Id of the vehicle service that is requested.
	 * @param startDate
	 *            The start date of the service.
	 * @return the booking with some filled in fields.
	 */
	public VehicleServiceBooking processBooking(String id, Date startDate) {
		VehicleService vehicleService = serviceRepository
				.findVehicleServiceById(id);
		if (vehicleService == null) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Invalid vehicle service.");
		}
		var endDate = new Date(startDate.getTime()
				+ TimeUnit.MINUTES.toMillis(vehicleService.getDuration()));
		var calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		// ensure service not booked
		var existingBookings = bookingRepository.query(vehicleService,
				startDate, endDate);
		int i = 0;
		for (@SuppressWarnings("unused") var b : existingBookings) {
			++i;
		}
		if (i > 0) {
			throw new OplsApiException(HttpStatus.CONFLICT,
					"Vehicle service already booked for this timeslot.");
		}

		// ensure parking lot not closed
		var todaySchedule = scheduleRepository
				.findActiveParkingLotScheduleByDay(Day
						.fromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
		if (todaySchedule == null
				|| todaySchedule.inScheduleOnDay(startDate, endDate)) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Cannot be serviced outside of opening hours.");
		}
		var booking = vehicleService.newBooking(startDate);
		booking.setBookingStatus(BookingStatus.REQUESTED);
		booking.setDateRange(startDate, endDate);
		booking.setVehicleService(vehicleService);
		booking.setCost(vehicleService.getFee());
		return booking;
	}

	/**
	 * Save a booking to the database.
	 * 
	 * @param booking
	 *            The booking to be saved.
	 * @param userAccountId
	 *            The user's account ID.
	 * @param licensePlate
	 *            The license plate of the vehicle.
	 * @return the saved entry.
	 */
	@Transactional
	public VehicleServiceBooking saveBooking(VehicleServiceBooking booking,
			Integer userAccountId, String licensePlate) {
		if (booking.getVehicleService() != null
				&& booking.getStartDate() != null) {
			booking.setBookingStatus(BookingStatus.CONFIRMED);
		}

		if (userAccountId != null) {
			booking.setCustomer(customerRepository
					.findCustomerByUserAccountId(userAccountId));
		}
		booking.setLicensePlate(licensePlate);
		booking.generateConfirmationNumber();
		return bookingRepository.save(booking);
	}

	/**
	 * Query for vehicle service bookings.
	 * 
	 * @param customerUserId
	 *            The ID of the user account to query for, null if do not apply
	 *            filter.
	 * @param vehicleServiceIds
	 *            The IDs of vehicle services.
	 * @param startDate
	 *            The start date of the query range.
	 * @param endDate
	 *            The end date of the query range.
	 * @return The queried results.
	 */
	public Iterable<DateRange> queryBookings(Integer customerUserId,
			Collection<String> vehicleServiceIds, Date startDate,
			Date endDate) {
		Integer customerId = null;
		if (customerUserId != null) {
			var customer = customerRepository.findCustomerByUserAccountId(customerUserId);
			customerId = customer == null
					? null
					: customer.getId();
		}
		
		return bookingRepository.query(customerId, vehicleServiceIds, startDate,
				endDate);
	}

}
