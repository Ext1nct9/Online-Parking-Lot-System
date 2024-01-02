package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
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
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.model.VehicleService.VehicleServiceBooking;
import ca.mcgill.ecse321.opls.repository.CustomerRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemScheduleRepository;
import ca.mcgill.ecse321.opls.repository.VehicleServiceBookingRepository;
import ca.mcgill.ecse321.opls.repository.VehicleServiceRepository;

@ExtendWith(MockitoExtension.class)
public class TestVehicleServiceBookingService {

    @Mock
    private ParkingLotSystemRepository configRepository;

    @Mock
    private VehicleServiceBookingRepository bookingRepository;

    @Mock
    private VehicleServiceRepository serviceRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ParkingLotSystemScheduleRepository pLSSRepository;


    @InjectMocks
    private VehicleServiceBookingService service;

    private static final String badVehicleServiceId = "Tire Change";
    private static final String goodId = "car-wash";

    private Date startDate = DateHelper.parseDate("2023-03-16 13:15:00");
    private Date startDate2 = DateHelper.parseDate("2023-03-16 12:15:00");
    private Integer userAccountId = 420;
    private VehicleService carWash;
	private Customer customer;
    private ParkingLotSystem config;
    private VehicleServiceBooking booking;
    private VehicleServiceBooking booking2;

	@BeforeEach
	public void setupMocks() {
		carWash = new VehicleService();
		carWash.setDisplayName("Car Wash");
		carWash.setDuration(15);
		carWash.setFee(15);
		carWash.setId("car-wash");
		booking = carWash.newBooking(startDate);
		booking2 = carWash.newBooking(startDate2);

		customer = new Customer();
		customer.overrideId(152);

		lenient().when(serviceRepository.findVehicleServiceById(goodId))
				.thenAnswer((InvocationOnMock invocation) -> carWash);

		lenient()
				.when(serviceRepository
						.findVehicleServiceById(badVehicleServiceId))
				.thenAnswer((InvocationOnMock invocation) -> null);

		config = new ParkingLotSystem();
		lenient().when(configRepository.getActiveParkingLotSystem())
				.thenAnswer((InvocationOnMock invocation) -> config);

		lenient()
				.when(pLSSRepository
						.findActiveParkingLotScheduleByDay(any(Day.class)))
				.thenAnswer((InvocationOnMockinvocation) -> config
						.addSchedule(Day.MONDAY, "00:00:00", "14:30:00"));

		lenient().when(bookingRepository.save(any(VehicleServiceBooking.class)))
				.thenAnswer((InvocationOnMock invocation) -> invocation
						.getArgument(0));

		lenient()
				.when(bookingRepository
						.findVehicleServiceBookingById(booking.getId()))
				.thenAnswer((InvocationOnMock invocation) -> booking);
		lenient()
				.when(bookingRepository
						.findVehicleServiceBookingByUuid(booking.getUuid()))
				.thenAnswer((InvocationOnMock invocation) -> booking);

		lenient()
				.when(customerRepository
						.findCustomerByUserAccountId(userAccountId))
				.thenAnswer((InvocationOnMock invocation) -> customer);
	}

    /**
     * Test getting bookings.
     */
    @Test
    public void testGetVehicleServiceBooking(){
        setupMocks();
        final var myStatus = BookingStatus.CONFIRMED;
        final var notMyStatus = BookingStatus.PAID;

        assertNotEquals(myStatus, notMyStatus);
        booking = carWash.newBooking(startDate);
        booking.generateConfirmationNumber();
        booking.setBookingStatus(myStatus);
        
        assertNotNull(booking);
        lenient()
                    .when(bookingRepository.findVehicleServiceBookingByUuid(booking.getUuid()))
                    .thenAnswer((InvocationOnMock invocation) -> booking);
        // get by id
        var response = service.getVehicleServiceBooking(booking.getId());
        assertNotNull(response);
        assertEquals(booking.getConfirmationNumber(), response.getConfirmationNumber());
        
        // get by uuid
        response = service.getVehicleServiceBooking(booking.getUuid());
        assertNotNull(response);
        assertEquals(booking.getConfirmationNumber(), response.getConfirmationNumber());

        // invalid request
        assertThrowsApiException(HttpStatus.NOT_FOUND, "Booking not found.", () -> service.getVehicleServiceBooking(UUID.randomUUID()));
    }

    /**
     * Test processing and validating a vehicle service booking request.
     */
	@Test
	public void testProcessVehicleServiceBooking() {
		setupMocks();
		final var badStartDate = DateHelper.parseDate("2023-03-16 14:31:15");
		var endDate = new Date(startDate.getTime()
				+ TimeUnit.MINUTES.toMillis(carWash.getDuration()));

		// valid request
		var response = service.processBooking(goodId, startDate);
		assertNotNull(response);
		assertEquals(carWash, response.getVehicleService());
		assertEquals(BookingStatus.REQUESTED, response.getBookingStatus());
		assertEquals(carWash.getFee(), response.getCost());
		assertEquals(carWash.getDuration(),
				(int) (response.getDateRange().getDurationMinutes()));

		// invalid request
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Invalid vehicle service.",
				() -> service.processBooking(badVehicleServiceId, startDate));

		// invalid request
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"Cannot be serviced outside of opening hours.",
				() -> service.processBooking(goodId, badStartDate));

		// invalid request
		var bookedDateRange = new DateRange(response.getStartDate(), response.getEndDate());
		lenient()
				.when(bookingRepository.query(carWash, startDate,
						endDate))
				.thenAnswer((InvocationOnMock invocation) -> Collections.singleton(bookedDateRange));
		assertThrowsApiException(HttpStatus.CONFLICT,
				"Vehicle service already booked for this timeslot.",
				() -> service.processBooking(goodId, startDate));
	}

    /**
     * Test save a vehicle service booking.
     */
    @Test
    public void testSaveVehicleServiceBooking(){
        customer = new Customer();
        String licensePlate = "1337";
        customer.setSavedLicensePlate(licensePlate);
        
        // valid request
        var response = service.saveBooking(booking, 420, licensePlate);
        assertNotNull(response);
        assertEquals(licensePlate, response.getLicensePlate());
        assertNotNull(response.getConfirmationNumber());
        assertEquals(BookingStatus.CONFIRMED, response.getBookingStatus());
        assertEquals(carWash.getFee(), response.getCost());
        assertEquals(customer.getId(), response.getCustomer().getId());
        assertEquals(carWash, response.getVehicleService());
        assertEquals(licensePlate, response.getLicensePlate());
        verify(bookingRepository, times(1)).save(response);

        // valid update request
        String newLicensePlate = "K9SAD";
        lenient()
                    .when(bookingRepository.findVehicleServiceBookingByUuid(booking.getUuid()))
                    .thenAnswer((InvocationOnMock invocation) -> booking);
        response = service.updateVehicleServiceBooking(booking.getUuid(), newLicensePlate, BookingStatus.PAID);
        assertNotNull(response);
        assertEquals(newLicensePlate, response.getLicensePlate());
        assertEquals(carWash, response.getVehicleService());
        assertEquals(BookingStatus.PAID, response.getBookingStatus());
        verify(bookingRepository, times(2)).save(response);

        // invalid update request
        assertThrowsApiException(HttpStatus.NOT_FOUND, "Booking not found.",
                () -> service.updateVehicleServiceBooking(UUID.randomUUID(), licensePlate, BookingStatus.CONFIRMED));
        verify(bookingRepository, times(2)).save(response);

        // delete booking
        service.deleteVehicleServiceBooking(response.getUuid());
        lenient()
                    .when(bookingRepository.findVehicleServiceBookingByUuid(booking.getUuid()))
                    .thenAnswer((InvocationOnMock invocation) -> null);
        assertThrowsApiException(HttpStatus.NOT_FOUND, "Booking not found.",
                () -> service.getVehicleServiceBooking(booking.getUuid()));
    }

	/**
	 * Test querying bookings.
	 */
	public void testQueryBookings() {
		var ids = Collections.singleton(carWash.getId());
		var dateRange1 = new DateRange(booking.getUuid(),
				carWash.getDisplayName(), booking.getStartDate(),
				booking.getEndDate());
		var dateRange2 = new DateRange(booking2.getUuid(),
				carWash.getDisplayName(), booking2.getStartDate(),
				booking2.getEndDate());
		lenient()
				.when(bookingRepository.query(customer.getId(), ids,
						any(Date.class), any(Date.class)))
				.thenAnswer((InvocationOnMock invocation) -> Collections
						.singleton(dateRange2));
		lenient()
				.when(bookingRepository.query(null, ids, any(Date.class),
						any(Date.class)))
				.thenAnswer((InvocationOnMock invocation) -> Arrays
						.asList(dateRange1, dateRange2));

		// query with user ID
		var results = service.queryBookings(userAccountId, ids, startDate2,
				startDate);
		int i = 0;
		for (var res : results) {
			assertEquals(carWash.getDisplayName(), res.name);
			assertEquals(booking.getUuid(), res.uuid);
			++i;
		}
		assertEquals(1, i);

		// query without user ID
		results = service.queryBookings(null, ids, startDate2, startDate);
		i = 0;
		for (var res : results) {
			assertEquals(carWash.getDisplayName(), res.name);
			if (res.uuid != booking.getUuid()
					&& res.uuid != booking2.getUuid()) {
				fail("Booking not recognized.");
			}
			++i;
		}
		assertEquals(2, i);
	}
    
}
