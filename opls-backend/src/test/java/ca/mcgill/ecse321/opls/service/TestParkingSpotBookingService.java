package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.DateHelper;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
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

/**
 * Test the ParkingSpotBookingService class.
 */
@ExtendWith(MockitoExtension.class)
public class TestParkingSpotBookingService {

	@Mock
	private ParkingLotSystemRepository configurationRepository;

	@Mock
	private ParkingLotSystemScheduleRepository scheduleRepository;

	@Mock
	private ParkingSpotRepository spotRepository;

	@Mock
	private ParkingSpotBookingRepository bookingRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private ParkingSpotBookingService service;

	private static final String MY_PS_ID = "A035";
	private static final String NOT_FOUND_PS_ID = "A023";
	private static final String BOOKED_PS_ID = "B035";
	private static final String RESERVED_PS_ID = "C035";

	private static final VehicleType MY_VEHICLE_TYPE = VehicleType.REGULAR;
	private static final VehicleType BAD_VEHICLE_TYPE = VehicleType.LARGE;

	private ParkingSpot ps;
	private ParkingSpot psBooked;
	private ParkingSpot psReserved;
	private ParkingLotSystem config;

	private void setupMocks() {
		ps = new ParkingSpot('A', 35, MY_VEHICLE_TYPE, ParkingSpotStatus.OPEN);
		psBooked = new ParkingSpot('B', 35, MY_VEHICLE_TYPE,
				ParkingSpotStatus.OPEN);
		psReserved = new ParkingSpot('C', 35, MY_VEHICLE_TYPE,
				ParkingSpotStatus.RESERVED);

		lenient().when(spotRepository.findParkingSpotById(MY_PS_ID))
				.thenAnswer((InvocationOnMock invocation) -> ps);
		lenient().when(bookingRepository.isParkingSpotBooked(ps))
				.then((InvocationOnMock invocation) -> false);

		lenient().when(spotRepository.findParkingSpotById(NOT_FOUND_PS_ID))
				.thenAnswer((InvocationOnMock invocation) -> null);

		lenient().when(spotRepository.findParkingSpotById(BOOKED_PS_ID))
				.thenAnswer((InvocationOnMock invocation) -> psBooked);
		lenient().when(bookingRepository.isParkingSpotBooked(psBooked))
				.then((InvocationOnMock invocation) -> true);

		lenient().when(spotRepository.findParkingSpotById(RESERVED_PS_ID))
				.thenAnswer((InvocationOnMock invocation) -> psReserved);
		lenient().when(bookingRepository.isParkingSpotBooked(psReserved))
				.then((InvocationOnMock invocation) -> false);

		config = new ParkingLotSystem();
		config.setIncrementFee(0.15);
		config.setIncrementTime(15);
		config.setMaxIncrementTime(60 * 12);
		config.setMonthlyFee(60.0);
		lenient().when(configurationRepository.getActiveParkingLotSystem())
				.thenAnswer((InvocationOnMock invocation) -> config);
	}

	/**
	 * Test getting parking spot bookings.
	 */
	@Test
	public void testGetBooking() {
		setupMocks();

		lenient().when(bookingRepository.getActiveBooking(ps))
				.thenAnswer((InvocationOnMock invocation) -> null);

		final var myStatus = BookingStatus.CONFIRMED;
		final var notMyStatus = BookingStatus.PAID;
		assertNotEquals(myStatus, notMyStatus);
		var booking = psBooked.newBooking();
		booking.generateConfirmationNumber();
		booking.setBookingStatus(myStatus);
		lenient().when(bookingRepository.getActiveBooking(psBooked))
				.thenAnswer((InvocationOnMock invocation) -> booking);
		lenient()
				.when(bookingRepository
						.findParkingSpotBookingByUuid(booking.getUuid()))
				.thenAnswer((InvocationOnMock invocation) -> booking);
		lenient()
				.when(bookingRepository
						.findParkingSpotBookingByConfirmationNumber(
								booking.getConfirmationNumber()))
				.thenAnswer((InvocationOnMock invocation) -> booking);
		lenient()
				.when(bookingRepository
						.findParkingSpotBookingsByBookingStatus(myStatus))
				.thenAnswer((
						InvocationOnMock invocation) -> (Iterable<ParkingSpotBooking>) Arrays
								.asList(booking));
		lenient()
				.when(bookingRepository
						.findParkingSpotBookingsByBookingStatus(notMyStatus))
				.thenAnswer((
						InvocationOnMock invocation) -> Collections.EMPTY_LIST);

		// fetch by parking spot
		var response = service.getBooking(ps);
		assertNull(response);

		// fetch by parking spot
		response = service.getBooking(psBooked);
		assertNotNull(response);
		assertEquals(booking.getConfirmationNumber(),
				response.getConfirmationNumber());

		// fetch by ID
		response = service.getBooking(booking.getUuid());
		assertNotNull(response);
		assertEquals(booking.getConfirmationNumber(),
				response.getConfirmationNumber());

		// fetch by ID
		assertThrowsApiException(HttpStatus.NOT_FOUND, "Booking not found.",
				() -> service.getBooking(UUID.randomUUID()));

		// fetch by confirmation number
		response = service.getBooking(booking.getConfirmationNumber());
		assertNotNull(response);
		assertEquals(booking.getUuid(), response.getUuid());

		// fetch by confirmation number
		assertThrowsApiException(HttpStatus.NOT_FOUND, "Booking not found.",
				() -> service.getBooking(UUID.randomUUID().toString()));

		// fetch by status
		var responses = service.getBookings(booking.getBookingStatus());
		assertNotNull(response);
		for (var b : responses) {
			assertEquals(booking.getConfirmationNumber(),
					b.getConfirmationNumber());
			assertEquals(booking.getUuid(), b.getUuid());
		}

		// fetch by status
		responses = service.getBookings(notMyStatus);
		assertEquals(0, ((List<ParkingSpotBooking>) responses).size());
	}

	/**
	 * Test determining if a parking spot is booked.
	 */
	@Test
	public void testAssertNotBooked() {
		setupMocks();

		// test valid request
		service.assertNotBooked(ps);

		// test invalid request
		assertThrowsApiException(HttpStatus.CONFLICT,
				"Parking spot already booked.",
				() -> service.assertNotBooked(psBooked));
	}

	/**
	 * Test processing and validating an incremental booking request.
	 */
	@Test
	public void testProcessIncrementalBooking() throws ParseException {
		setupMocks();

		lenient()
				.when(scheduleRepository
						.findActiveParkingLotScheduleByDay(any(Day.class)))
				.thenAnswer((InvocationOnMock invocation) -> config
						.addSchedule(Day.MONDAY, "00:00:00", "23:59:59"));

		// valid request
		final var startDate = DateHelper.parseDate("2023-03-16 14:15:15");
		var duration = config.getIncrementTime() - 1;
		var expectedEndDate = DateRange.rangeWithStartAndDuration(startDate,
				config.getIncrementTime(), TimeUnit.MINUTES).endDate;
		var response = service.processIncrementalBooking(ps, startDate,
				duration, MY_VEHICLE_TYPE);
		assertNotNull(response);
		assertEquals(MY_PS_ID, response.getParkingSpot().getId());
		assertEquals(BookingStatus.REQUESTED, response.getBookingStatus());
		assertEquals(startDate, response.getStartDate());
		assertEquals(expectedEndDate, response.getEndDate());
		assertEquals(config.getIncrementFee(), response.getCost());

		// invalid request
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Invalid vehicle type.",
				() -> service.processIncrementalBooking(ps, startDate, duration,
						BAD_VEHICLE_TYPE));

		// invalid request
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Parking spot is reserved.",
				() -> service.processIncrementalBooking(psReserved, startDate,
						duration, MY_VEHICLE_TYPE));

		// invalid request
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Cannot park for more than " + config.getMaxIncrementTime()
						+ " minutes.",
				() -> service.processIncrementalBooking(ps, startDate,
						config.getMaxIncrementTime() + 5, MY_VEHICLE_TYPE));

		// invalid request
		lenient()
				.when(scheduleRepository
						.findActiveParkingLotScheduleByDay(any(Day.class)))
				.thenAnswer((InvocationOnMock invocation) -> config
						.addSchedule(Day.MONDAY, "00:00:00", "14:30:00"));
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Cannot park outside of opening hours.",
				() -> service.processIncrementalBooking(ps, startDate, 180,
						MY_VEHICLE_TYPE));
	}

	/**
	 * Test processing and validating a monthly booking request.
	 */
	@Test
	public void testProcessMonthlyBooking() {
		setupMocks();

		lenient()
				.when(spotRepository.queryUnbookedCount(null,
						Arrays.asList(ParkingSpotStatus.RESERVED), null))
				.thenAnswer((InvocationOnMock invocation) -> 100);

		final var startDate = DateHelper.parseDate("2023-03-16 14:15:15");
		var calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.MONTH, 1);
		var expectedEndDate = calendar.getTime();

		// valid request
		var response = service.processMonthlyBooking(startDate,
				MY_VEHICLE_TYPE);
		assertNotNull(response);
		assertEquals(BookingStatus.REQUESTED, response.getBookingStatus());
		assertEquals(config.getMonthlyFee(), response.getCost());
		assertNull(response.getParkingSpot());
		assertEquals(startDate, response.getStartDate());
		assertEquals(expectedEndDate, response.getEndDate());

		// invalid request
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Invalid vehicle type.", () -> service
						.processMonthlyBooking(startDate, BAD_VEHICLE_TYPE));

		// invalid request
		lenient()
				.when(spotRepository.queryUnbookedCount(null,
						Arrays.asList(ParkingSpotStatus.RESERVED), null))
				.thenAnswer((InvocationOnMock invocation) -> 0);
		assertThrowsApiException(HttpStatus.CONFLICT,
				"No open monthly spots found.", () -> service
						.processMonthlyBooking(startDate, MY_VEHICLE_TYPE));
	}

	/**
	 * Test saving booking request.
	 */
	@Test
	public void testSaveBooking() {
		int userId = 12;
		var customer = new Customer();
		customer.setSavedLicensePlate("ABCD");

		ps = new ParkingSpot('A', 35);
		var booking = ps.newBooking();

		lenient().when(bookingRepository.save(any(ParkingSpotBooking.class)))
				.thenAnswer((InvocationOnMock invocation) -> invocation
						.getArgument(0));
		lenient()
				.when(bookingRepository
						.findParkingSpotBookingByUuid(any(UUID.class)))
				.thenAnswer((InvocationOnMock invocation) -> null);
		lenient()
				.when(bookingRepository
						.findParkingSpotBookingByUuid(booking.getUuid()))
				.thenAnswer((InvocationOnMock invocation) -> booking);
		lenient().when(customerRepository.findCustomerByUserAccountId(userId))
				.thenAnswer((InvocationOnMock invocation) -> customer);

		// valid request
		var resp = service.saveBooking(booking, userId, "ABCD");
		assertNotNull(resp);
		assertEquals("ABCD", resp.getLicensePlate());
		assertNotNull(resp.getConfirmationNumber());
		assertEquals(BookingStatus.CONFIRMED, resp.getBookingStatus());
		assertEquals(customer.getSavedLicensePlate(),
				resp.getCustomer().getSavedLicensePlate());
		verify(bookingRepository, times(1)).save(resp);

		// valid patch request
		var ps2 = new ParkingSpot('B', 36);
		lenient().when(bookingRepository.isParkingSpotBooked(ps2))
				.then((InvocationOnMock invocation) -> false);
		resp = service.patchBooking(booking.getUuid(), ps2, BookingStatus.PAID);
		assertNotNull(resp);
		assertEquals(ps2.getId(), resp.getParkingSpot().getId());
		assertEquals(BookingStatus.PAID, resp.getBookingStatus());
		verify(bookingRepository, times(2)).save(resp);

		// invalid patch request
		assertThrowsApiException(HttpStatus.NOT_FOUND, "Booking not found.",
				() -> service.patchBooking(UUID.randomUUID(), ps2,
						BookingStatus.CONFIRMED));
		verify(bookingRepository, times(2)).save(resp);

		// delete booking
		resp = service.deleteBooking(resp.getUuid());
		assertNotNull(resp);
		assertEquals("ABCD", resp.getLicensePlate());
		verify(bookingRepository, times(1)).delete(resp);
	}

}

/**
 *				 Sussy baka
 *
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣤⣤⣤⣀⣀⣀⣀⡀⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⠟⠉⠉⠉⠉⠉⠉⠉⠙⠻⢶⣄⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣾⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⠙⣷⡀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣸⡟⠀⣠⣶⠛⠛⠛⠛⠛⠛⠳⣦⡀⠀⠘⣿⡄⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⠁⠀⢹⣿⣦⣀⣀⣀⣀⣀⣠⣼⡇⠀⠀⠸⣷⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⡏⠀⠀⠀⠉⠛⠿⠿⠿⠿⠛⠋⠁⠀⠀⠀⠀ ⣿
 * ⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⢻⡇⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⣸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⢸⡇⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⣿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⢸⣧⠀
 * ⠀⠀⠀⠀⠀⠀⠀⢸⡿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀   ⣿⠀
 * ⠀⠀⠀⠀⠀⠀⠀⣾⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⠀⣿⠀
 * ⠀⠀⠀⠀⠀⠀⠀⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀  ⣿⠀
 * ⠀⠀⠀⠀⠀⠀⢰⣿⠀⠀⠀⠀⣠⡶⠶⠿⠿⠿⠿⢷⣦⠀⠀⠀⠀ ⠀⠀⣿⠀
 * ⠀⠀⣀⣀⣀⠀⣸⡇⠀⠀⠀⠀⣿⡀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀ ⠀⠀⣿⠀
 * ⣠⡿⠛⠛⠛⠛⠻⠀⠀⠀⠀⠀⢸⣇⠀⠀⠀⠀⠀⠀⣿⠇⠀⠀⠀ ⠀ ⣿⠀
 * ⢻⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⡟⠀⠀    ⣤⣴⣿⠀⠀⠀⠀⠀ ⣿⠀
 * ⠈⠙⢷⣶⣦⣤⣤⣤⣴⣶⣾⠿⠛     ⣶⡟⠉⠀⠀⠀⠀⠀⠀ ⡟⠀
 * ⠀                     ⣿⣆⡀  ⠀⢀⣠⣴⡾⠃⠀
 * ⠀                      ⠈⠛⠻⠿⠟⠋
 */
