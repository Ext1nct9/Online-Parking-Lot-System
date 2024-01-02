package ca.mcgill.ecse321.opls.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotBooking;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.repository.CustomerRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemScheduleRepository;
import ca.mcgill.ecse321.opls.repository.ParkingSpotBookingRepository;
import ca.mcgill.ecse321.opls.repository.ParkingSpotRepository;
import jakarta.transaction.Transactional;

@Service
public class ParkingSpotBookingService {

	@Autowired
	private ParkingLotSystemRepository configurationRepository;

	@Autowired
	private ParkingLotSystemScheduleRepository scheduleRepository;

	@Autowired
	private ParkingSpotRepository spotRepository;

	@Autowired
	private ParkingSpotBookingRepository bookingRepository;

	@Autowired
	private CustomerRepository customerRepository;

	/**
	 * Fetch the current booking for the parking spot.
	 * 
	 * @param spot
	 *            The parking spot to check.
	 * @return The ParkingSpotBooking entity, null if not found.
	 */
	public ParkingSpotBooking getBooking(ParkingSpot spot) {
		return bookingRepository.getActiveBooking(spot);
	}

	/**
	 * Fetch the parking spot booking.
	 * 
	 * @param uuid
	 *            The ID of the booking.
	 * @return The ParkingSpotBooking entity.
	 * @throws OplsApiException
	 *             if the booking is not found.
	 */
	public ParkingSpotBooking getBooking(UUID uuid) {
		var booking = bookingRepository.findParkingSpotBookingByUuid(uuid);

		if (booking == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Booking not found.");
		}

		return booking;
	}

	/**
	 * Fetch the parking spot booking.
	 * 
	 * @param confirmationNumber
	 *            The confirmation number of the booking.
	 * @return The ParkingSpotBooking entity.
	 * @throws OplsApiException
	 *             if the booking is not found.
	 */
	public ParkingSpotBooking getBooking(String confirmationNumber) {
		var booking = bookingRepository
				.findParkingSpotBookingByConfirmationNumber(confirmationNumber);

		if (booking == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Booking not found.");
		}

		return booking;
	}

	/**
	 * Fetch bookings by status.
	 * 
	 * @param status
	 *            The status to filter by.
	 * @return A list of bookings with the status.
	 */
	public Iterable<ParkingSpotBooking> getBookings(BookingStatus status) {
		return bookingRepository.findParkingSpotBookingsByBookingStatus(status);
	}

	/**
	 * Ensure a parking spot is not booked.
	 * 
	 * @param spot
	 *            The ParkingSpot to check.
	 * @throws OplsApiException
	 *             if the spot is booked.
	 */
	public void assertNotBooked(ParkingSpot spot) {
		if (bookingRepository.isParkingSpotBooked(spot)) {
			throw new OplsApiException(HttpStatus.CONFLICT,
					"Parking spot already booked.");
		}
	}

	/**
	 * Validate a parking spot booking request.
	 * 
	 * @param spot
	 *            The requested parking spot.
	 * @param startDate
	 *            The start date of the reservation.
	 * @param duration
	 *            Length of the requested booking in minutes.
	 * @param vehicleType
	 *            The type of vehicle the user is trying to park with.
	 * @return The the booking with some filled in fields.
	 */
	public ParkingSpotBooking processIncrementalBooking(ParkingSpot spot,
			Date startDate, int duration, VehicleType vehicleType) {
		// validate spot
		if (vehicleType != spot.getVehicleType()) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Invalid vehicle type.");
		}
		if (spot.getParkingSpotStatus() == ParkingSpotStatus.RESERVED) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Parking spot is reserved.");
		}

		assertNotBooked(spot);

		// get current configuration
		var config = configurationRepository.getActiveParkingLotSystem();

		// round up duration
		int numIncrements = duration / config.getIncrementTime();
		if (duration % config.getIncrementTime() > 0) {
			numIncrements++;
		}
		duration = numIncrements * config.getIncrementTime();

		if (duration > config.getMaxIncrementTime()) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Cannot park for more than " + config.getMaxIncrementTime()
							+ " minutes.");
		}

		// compute time in milliseconds
		var endDate = new Date(
				startDate.getTime() + TimeUnit.MINUTES.toMillis(duration));

		// get schedule for today
		var calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		var todaySchedule = scheduleRepository
				.findActiveParkingLotScheduleByDay(Day
						.fromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
		if (todaySchedule == null
				|| todaySchedule.inScheduleOnDay(startDate, endDate)) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Cannot park outside of opening hours.");
		}

		// create booking entry
		var booking = spot.newBooking();
		booking.setBookingStatus(BookingStatus.REQUESTED);
		booking.setDateRange(startDate, endDate);
		booking.setParkingSpot(spot);
		booking.setCost((double) numIncrements * config.getIncrementFee());

		return booking;
	}

	/**
	 * Validate a monthly parking spot booking request.
	 * 
	 * @param startDate
	 *            The start date of the reservation.
	 * @param vehicleType
	 *            The type of vehicle the user is trying to park with.
	 * @return The the booking with some filled in fields.
	 */
	public ParkingSpotBooking processMonthlyBooking(Date startDate,
			VehicleType vehicleType) {
		// validate vehicle type
		if (vehicleType != VehicleType.REGULAR) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST,
					"Invalid vehicle type.");
		}

		// get current configuration
		var config = configurationRepository.getActiveParkingLotSystem();

		// determine if open spots available
		if (spotRepository.queryUnbookedCount(null,
				Arrays.asList(ParkingSpotStatus.RESERVED), null) == 0) {
			throw new OplsApiException(HttpStatus.CONFLICT,
					"No open monthly spots found.");
		}

		// calculate end date
		var calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.MONTH, 1);
		var endDate = calendar.getTime();

		// create booking entry
		var booking = new ParkingSpotBooking();
		booking.setBookingStatus(BookingStatus.REQUESTED);
		booking.setDateRange(startDate, endDate);
		booking.setCost(config.getMonthlyFee());

		return booking;
	}

	/**
	 * Save a parking spot booking to the database.
	 * 
	 * @param booking
	 *            The booking.
	 * @param userAccountId
	 *            The ID of the user making the booking, null if it does not
	 *            exist.
	 * @param licensePlate
	 *            The license plate of the parked user.
	 * @return The saved entry.
	 */
	@Transactional
	public ParkingSpotBooking saveBooking(ParkingSpotBooking booking,
			Integer userAccountId, String licensePlate) {
		// determine if yet to be assigned
		if (booking.getParkingSpot() != null) {
			booking.setBookingStatus(BookingStatus.CONFIRMED);
		}

		// attach registered user
		if (userAccountId != null) {
			booking.setCustomer(customerRepository
					.findCustomerByUserAccountId(userAccountId));
		}

		booking.setLicensePlate(licensePlate);

		// generate confirmation number
		booking.generateConfirmationNumber();

		return bookingRepository.save(booking);
	}

	/**
	 * Modify a spot booking.
	 * 
	 * @param bookingId
	 *            The ID of the booking.
	 * @param newSpot
	 *            The parking spot to associate with the booking, null if do not
	 *            change.
	 * @param newStatus
	 *            The status to set for the booking, null if do not change.
	 * @return The updated entity.
	 */
	@Transactional
	public ParkingSpotBooking patchBooking(UUID bookingId, ParkingSpot newSpot,
			BookingStatus newStatus) {
		var booking = getBooking(bookingId);

		// update spot
		if (newSpot != null) {
			assertNotBooked(newSpot);
			booking.setParkingSpot(newSpot);
		}

		// update status
		if (newStatus != null) {
			booking.setBookingStatus(newStatus);
		}

		// save
		return bookingRepository.save(booking);
	}

	/**
	 * Delete a parking spot booking.
	 * 
	 * @param bookingId
	 *            The ID of the booking.
	 * @return The deleted entity.
	 */
	@Transactional
	public ParkingSpotBooking deleteBooking(UUID bookingId) {
		var booking = getBooking(bookingId);

		bookingRepository.delete(booking);

		return booking;
	}
	
	/**
	 * Get a customer's parking spot bookings.
	 * 
	 * @param userAccountId
	 *            The ID of the user to request.
	 * @return The fetched bookings.
	 */
	public Iterable<ParkingSpotBooking> getCustomerBookings(int userAccountId) {
		var customer = customerRepository
				.findCustomerByUserAccountId(userAccountId);

		return customer == null
				? Arrays.asList()
				: bookingRepository.getCustomerActiveBookings(customer);
	}

}
