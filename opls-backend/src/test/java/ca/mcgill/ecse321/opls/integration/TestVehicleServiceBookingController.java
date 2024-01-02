package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.mcgill.ecse321.opls.DateHelper;
import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.dto.OplsApiErrorResponseDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingQueryRequestDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingQueryResponseDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.service.booking.UpdateServiceBookingRequestDto;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.VehicleServiceService;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestVehicleServiceBookingController extends OplsApiTester {
	
	@Autowired
	private OplsStartupService startupService;

	private VehicleService carWash;

	@Autowired
	private VehicleServiceService vehicleServiceService;

	private static final int duration = 15;
	private static final double fee = 50;
	private static final String displayName = "Car Wash";
	private static final String licensePlate = "123456";
	private static final Date startDate = DateHelper
			.parseDate("2023-03-16 12:00:00");
	private static final Date startDate2 = DateHelper
			.parseDate("2023-03-16 13:00:00");
	
	@Override
	@PostConstruct
	public void specifyAuthentication() {
		this.addDefaultClaim(OAuthClaim.CUSTOMER);
		this.addDefaultClaim(OAuthClaim.EMPLOYEE);
	}
	
	@AfterEach
	public void clearDatabase() throws Exception {
		startupService.startupTest();
	}

	@BeforeEach
	public void setupData() {
		startupService.initializeConfiguration();
		startupService.initializeParkingLotStructure();
	}

	@Override
	@BeforeEach
	public void setupTestData() {
		carWash = vehicleServiceService.createVehicleService(displayName,
					duration, fee);
	}

	public static void assertDtoEquals(ServiceBookingDto expected,
			ServiceBookingDto actual) {
		assertEquals(expected.creditCardNumber, actual.creditCardNumber);
		assertEquals(expected.licensePlate, actual.licensePlate);
		assertEquals(expected.vehicleServiceId, actual.vehicleServiceId);
		assertEquals(expected.uuid, actual.uuid);
		assertEquals(expected.confirmationNumber, actual.confirmationNumber);
		assertEquals(expected.cost, actual.cost);
		assertEquals(expected.endDate, actual.endDate);
		assertEquals(expected.startDate, actual.startDate);
		assertEquals(expected.status, actual.status);
	}

	/**
	 * Test endpoint POST /service/booking.
	 */
	@Test
	public void testVehicleServiceBookingRequest() {
		// valid request
		var req = new ServiceBookingRequestDto(licensePlate, "1234567890123456",
				startDate);

		var endpoint = "/service/" + carWash.getId() + "/booking";
		var resp = this.exchange(HttpMethod.POST, endpoint, req,
				ServiceBookingDto.class, HttpStatus.OK).getBody();
		assertNotNull(resp.uuid);
		assertNotNull(resp.confirmationNumber);
		assertEquals(BookingStatus.CONFIRMED, resp.status);
		assertEquals(licensePlate, resp.licensePlate);
		assertEquals(carWash.getFee(), resp.cost);
		assertEquals(duration, new DateRange(resp.startDate, resp.endDate)
				.getDurationMinutes());
		
		// test making same booking
		assertReturnsError(HttpStatus.CONFLICT, "Vehicle service already booked for this timeslot.", null, HttpMethod.POST, endpoint, req,
				null);

		// test invalid input
		req.creditCardNumber = "1234890123456";
		req.licensePlate = "asdfaasdfasdfasdfasd";
		var error = assertReturnsError(HttpStatus.BAD_REQUEST,
				"Invalid request body.", null, HttpMethod.POST, endpoint, req,
				null);
		assertTrue(error.hasField("creditCardNumber",
				"Invalid credit card number."));
		assertTrue(error.hasField("licensePlate", "Invalid license plate."));
	}

	/**
	 * Test endpoint GET /service/booking.
	 */
	@Test
	public void testGetVehicleServiceBooking() {
		// Create booking
		var req = new ServiceBookingRequestDto(licensePlate, "1234567890123456",
				startDate);
		var endpoint = "/service/" + carWash.getId() + "/booking";
		var bookingReq = this.exchange(HttpMethod.POST, endpoint, req,
				ServiceBookingDto.class, HttpStatus.OK).getBody();

		// Create endpoint variables
		var endpointforUUID = "/service/booking/" + bookingReq.uuid;
		var endpointforUUIDBad = "/service/booking/"
				+ UUID.randomUUID().toString();
		var endpointforCN = "/service/booking/byConfirmation/"
				+ bookingReq.confirmationNumber;
		var endpointforCNBad = "/service/booking/byConfirmation/1";
		// Check Get booking by uuid
		var getResp = this.exchange(HttpMethod.GET, endpointforUUID,
				ServiceBookingDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(bookingReq, getResp);
		// Get booking with bad uuid
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Booking not found.",
				null, HttpMethod.GET, endpointforUUIDBad, null);
		// Check Get booking by confirmation number
		var getRestp = this.exchange(HttpMethod.GET, endpointforCN,
				ServiceBookingDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(bookingReq, getRestp);
		// Check Get booking by bad confirmation number
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Booking not found.",
				null, HttpMethod.GET, endpointforCNBad, null);
	}
	
	/**
	 * Test endpoint POST /service/booking/search.
	 */
	@Test
	public void testQueryVehicleServiceBookings() {
		// create bookings
		var endpoint = "/service/" + carWash.getId() + "/booking";
		var req = new ServiceBookingRequestDto(licensePlate, "1234567890123456",
				startDate);
		var booking = this.exchange(HttpMethod.POST, endpoint, req,
				ServiceBookingDto.class, HttpStatus.OK).getBody();

		// make query
		endpoint = "/service/booking/search";
		var queryReq = new ServiceBookingQueryRequestDto();
		queryReq.queryOwn = true;
		queryReq.vehicleServiceIds = Collections.singleton(carWash.getId());
		queryReq.startDate = startDate;
		queryReq.endDate = startDate2;
		var response = this
				.exchange(HttpMethod.POST, endpoint, queryReq,
						ServiceBookingQueryResponseDto.class, HttpStatus.OK)
				.getBody();
		assertEquals(1, response.count);
		for (var foundBooking : response.bookings) {
			assertEquals(booking.uuid, foundBooking.uuid);
			assertEquals(startDate, foundBooking.startDate);
		}
		
		// make query again
		queryReq.queryOwn = false;
		queryReq.vehicleServiceIds = null;
		response = this
				.exchange(HttpMethod.POST, endpoint, queryReq,
						ServiceBookingQueryResponseDto.class, HttpStatus.OK)
				.getBody();
		assertEquals(1, response.count);
		for (var foundBooking : response.bookings) {
			assertEquals(booking.uuid, foundBooking.uuid);
			assertEquals(startDate, foundBooking.startDate);
		}
		
		// make query again
		queryReq.vehicleServiceIds = Arrays.asList(carWash.getId(), "tire-inflation");
		response = this
				.exchange(HttpMethod.POST, endpoint, queryReq,
						ServiceBookingQueryResponseDto.class, HttpStatus.OK)
				.getBody();
		assertEquals(1, response.count);
		for (var foundBooking : response.bookings) {
			assertEquals(booking.uuid, foundBooking.uuid);
			assertEquals(carWash.getDisplayName(), foundBooking.name);
			assertEquals(startDate, foundBooking.startDate);
		}

		// invalid query
		queryReq.startDate = null;
		queryReq.endDate = null;
		var error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
				"Invalid request body.", null, HttpMethod.POST, endpoint, queryReq, null);
		assertTrue(error.hasField("startDate", "Must be populated."));
		assertTrue(error.hasField("endDate", "Must be populated."));
	}

	/**
	 * Test endpoint PATCH /service/booking.
	 */
	@Test
	public void testUpdateVehicleServiceBooking() {
		// Create booking
		var req = new ServiceBookingRequestDto(licensePlate, "1234567890123456",
				startDate);
		var endpoint = "/service/" + carWash.getId() + "/booking";
		var bookingReq = this.exchange(HttpMethod.POST, endpoint, req,
		ServiceBookingDto.class, HttpStatus.OK).getBody();
		
		// Create endpoint and update request
		var endpointUpdate = "/service/booking/"+bookingReq.uuid;
		var updateReq = new UpdateServiceBookingRequestDto();
		updateReq.licensePlate = "123456";
		updateReq.bookingStatus = BookingStatus.CONFIRMED;

		// Update endpoint
		var updateResp = this.exchange(HttpMethod.PATCH, endpointUpdate, updateReq, ServiceBookingDto.class, HttpStatus.OK).getBody();
		assertEquals(bookingReq.uuid, updateResp.uuid);
		assertEquals(updateReq.licensePlate, updateResp.licensePlate);
		assertEquals(updateReq.bookingStatus, updateResp.status);

		// Delete the booking
		this.exchange(HttpMethod.DELETE, endpointUpdate, ServiceBookingDto.class, HttpStatus.OK);
		
		// Delete an invalid booking
		var endpointBad = "/service/booking/"+UUID.randomUUID().toString();
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Booking not found.", null, HttpMethod.DELETE, endpointBad, null);
	}

}
