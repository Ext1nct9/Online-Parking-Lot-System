package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
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

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.dto.CustomerDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.IncrementalSpotBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.MonthlySpotBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.PatchSpotBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.SpotBookingResponseDto;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestSpotBookingController extends OplsApiTester {

	@Autowired
	private OplsStartupService startupService;

	@Override
	@PostConstruct
	public void specifyAuthentication() {
		this.addDefaultClaim(OAuthClaim.CUSTOMER);
		this.addDefaultClaim(OAuthClaim.EMPLOYEE);
	}

	@Override
	@BeforeEach
	public void setupTestData() {
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

	public static IncrementalSpotBookingRequestDto newIncrementalBookingRequest(
			int duration, String parkingSpotId, VehicleType vehicleType) {
		var req = new IncrementalSpotBookingRequestDto();
		req.creditCardNumber = "1234123412341234";
		req.duration = duration;
		req.licensePlate = "ABCD";
		req.parkingSpotId = parkingSpotId;
		req.vehicleType = vehicleType;
		return req;
	}

	public static void assertDtoEquals(SpotBookingResponseDto expected,
			SpotBookingResponseDto actual) {
		assertEquals(expected.uuid, actual.uuid);
		assertEquals(expected.confirmationNumber, actual.confirmationNumber);
		assertEquals(expected.cost, actual.cost);
		assertEquals(expected.endDate, actual.endDate);
		assertEquals(expected.parkingSpotId, actual.parkingSpotId);
		assertEquals(expected.startDate, actual.startDate);
		assertEquals(expected.status, actual.status);
	}

	/**
	 * Test endpoint POST /spot/booking/incremental.
	 */
	@Test
	public void testIncrementalBookingRequest() {
		// valid request
		var req = newIncrementalBookingRequest(30, "A035", VehicleType.REGULAR);

		var resp = this.exchange(HttpMethod.POST, "/spot/booking/incremental",
				req, SpotBookingResponseDto.class, HttpStatus.OK).getBody();
		assertNotNull(resp.uuid);
		assertNotNull(resp.confirmationNumber);
		assertEquals(BookingStatus.CONFIRMED, resp.status);
		assertEquals("A035", resp.parkingSpotId);
		assertEquals(0.50, resp.cost);
		assertEquals(30, new DateRange(resp.startDate, resp.endDate)
				.getDurationMinutes());

		req.parkingSpotId = "A0";
		req.creditCardNumber = "123";
		req.licensePlate = "AAAAAAAAAAAAAAAAAAAAA";
		req.vehicleType = null;
		req.duration = 0;
		var error = assertReturnsError(HttpStatus.BAD_REQUEST,
				"Invalid request body.", null, HttpMethod.POST,
				"/spot/booking/incremental", req, null);
		assertTrue(error.hasField("parkingSpotId", "Invalid parking spot ID."));
		assertTrue(error.hasField("creditCardNumber",
				"Invalid credit card number."));
		assertTrue(error.hasField("licensePlate", "Invalid license plate."));
		assertTrue(error.hasField("vehicleType", "Must be populated."));
		assertTrue(error.hasField("duration", "Duration must be positive."));
	}

	/**
	 * Test endpoint POST /spot/booking/monthly.
	 */
	@Test
	public void testMonthlyBookingRequest() {
		// valid request
		var req = new MonthlySpotBookingRequestDto();
		req.creditCardNumber = "1234123412341234";
		req.licensePlate = "ABCD";
		req.vehicleType = VehicleType.REGULAR;

		var resp = this.exchange(HttpMethod.POST, "/spot/booking/monthly", req,
				SpotBookingResponseDto.class, HttpStatus.OK).getBody();
		assertNotNull(resp.uuid);
		assertNotNull(resp.confirmationNumber);
		assertEquals(BookingStatus.PAID, resp.status);
		assertNull(resp.parkingSpotId);
		assertEquals(60.00, resp.cost);

		var startCalendar = Calendar.getInstance();
		startCalendar.setTime(resp.startDate);
		startCalendar.add(Calendar.MONTH, 1);
		assertEquals(resp.endDate.getTime(), startCalendar.getTimeInMillis());

		// invalid request
		req.creditCardNumber = "123";
		req.licensePlate = "AAAAAAAAAAAAAAAAAAAAA";
		req.vehicleType = null;
		var error = assertReturnsError(HttpStatus.BAD_REQUEST,
				"Invalid request body.", null, HttpMethod.POST,
				"/spot/booking/monthly", req, null);
		assertTrue(error.hasField("creditCardNumber",
				"Invalid credit card number."));
		assertTrue(error.hasField("licensePlate", "Invalid license plate."));
		assertTrue(error.hasField("vehicleType", "Must be populated."));
	}

	/**
	 * Test getting bookings.
	 */
	@Test
	public void testGetBooking() {
		final String psId = "A035";
		final String psId2 = "A036";
		assertNotEquals(psId, psId2);

		final BookingStatus myStatus = BookingStatus.CONFIRMED;
		final BookingStatus notMyStatus = BookingStatus.PAID;
		assertNotEquals(myStatus, notMyStatus);
		
		// set user as customer
		this.exchange(HttpMethod.POST, "/customer", new CustomerDto(), CustomerDto.class, HttpStatus.OK);
		this.apiClient.refreshAuthentication();

		// make a booking
		var bookingReq = newIncrementalBookingRequest(30, psId,
				VehicleType.REGULAR);
		var postResp = this
				.exchange(HttpMethod.POST, "/spot/booking/incremental",
						bookingReq, SpotBookingResponseDto.class, HttpStatus.OK)
				.getBody();
		assertNotNull(postResp.uuid);
		assertEquals(myStatus, postResp.status);
		assertEquals(psId, postResp.parkingSpotId);

		// fetch active booking
		var endpoint = "/spot/" + psId + "/booking";
		var getResp = this.exchange(HttpMethod.GET, endpoint,
				SpotBookingResponseDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(postResp, getResp);

		// fetch for spot without booking
		endpoint = "/spot/" + psId2 + "/booking";
		this.exchange(HttpMethod.GET, endpoint, SpotBookingResponseDto.class,
				HttpStatus.NOT_FOUND);

		// fetch booking by ID
		endpoint = "/spot/booking/" + postResp.uuid;
		getResp = this.exchange(HttpMethod.GET, endpoint,
				SpotBookingResponseDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(postResp, getResp);

		// fetch non-existent ID
		endpoint = "/spot/booking/" + UUID.randomUUID().toString();
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Booking not found.",
				null, HttpMethod.GET, endpoint, null);

		// fetch booking by confirmation number
		endpoint = "/spot/booking/byConfirmation/"
				+ postResp.confirmationNumber;
		getResp = this.exchange(HttpMethod.GET, endpoint,
				SpotBookingResponseDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(postResp, getResp);

		// fetch booking by non-existent confirmation number
		endpoint = "/spot/booking/byConfirmation/"
				+ UUID.randomUUID().toString();
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Booking not found.",
				null, HttpMethod.GET, endpoint, null);

		var classObj = new ArrayList<LinkedHashMap<?, ?>>().getClass();

		// fetch bookings by status
		endpoint = "/spot/booking/byStatus/" + myStatus.toString();
		var responses = this
				.exchange(HttpMethod.GET, endpoint, classObj, HttpStatus.OK)
				.getBody();
		var i = 0;
		ObjectMapper objectMapper = new ObjectMapper();
		for (var obj : responses) {
			++i;
			assertDtoEquals(postResp, objectMapper.convertValue(obj,
					SpotBookingResponseDto.class));
		}
		assertEquals(1, i);

		// fetch bookings by status
		endpoint = "/spot/booking/byStatus/" + notMyStatus.toString();
		responses = this
				.exchange(HttpMethod.GET, endpoint, classObj, HttpStatus.OK)
				.getBody();
		i = 0;
		for (@SuppressWarnings("unused")
		var obj : responses) {
			++i;
		}
		assertEquals(0, i);
		
		// get customer's active bookings
		responses = this
				.exchange(HttpMethod.GET, "/customer/spot/booking", classObj, HttpStatus.OK)
				.getBody();
		i = 0;
		for (var obj : responses) {
			++i;
			assertDtoEquals(postResp, objectMapper.convertValue(obj,
					SpotBookingResponseDto.class));
		}
		assertEquals(1, i);
	}

	/**
	 * Test updating bookings
	 */
	@Test
	public void testUpdateBooking() {
		final String psId = "A035";
		final String psId2 = "A036";
		assertNotEquals(psId, psId2);

		final BookingStatus myStatus = BookingStatus.CONFIRMED;
		final BookingStatus notMyStatus = BookingStatus.PAID;
		assertNotEquals(myStatus, notMyStatus);
		
		// set user as customer
		this.exchange(HttpMethod.POST, "/customer", new CustomerDto(), CustomerDto.class, HttpStatus.OK);
		this.apiClient.refreshAuthentication();

		// make a booking
		var bookingReq = newIncrementalBookingRequest(30, psId,
				VehicleType.REGULAR);
		var postResp = this
				.exchange(HttpMethod.POST, "/spot/booking/incremental",
						bookingReq, SpotBookingResponseDto.class, HttpStatus.OK)
				.getBody();
		assertNotNull(postResp.uuid);
		assertEquals(myStatus, postResp.status);
		assertEquals(psId, postResp.parkingSpotId);

		// update the booking
		var endpoint = "/spot/booking/" + postResp.uuid;
		var request = new PatchSpotBookingRequestDto();
		request.parkingSpotId = psId2;
		request.status = notMyStatus;
		var patchResp = this.exchange(HttpMethod.PATCH, endpoint, request,
				SpotBookingResponseDto.class, HttpStatus.OK).getBody();
		assertEquals(postResp.uuid, patchResp.uuid);
		assertEquals(postResp.confirmationNumber, patchResp.confirmationNumber);
		assertEquals(psId2, patchResp.parkingSpotId);
		assertEquals(notMyStatus, patchResp.status);

		// delete the booking
		var delResp = this.exchange(HttpMethod.DELETE, endpoint, SpotBookingResponseDto.class,
				HttpStatus.OK).getBody();
		assertEquals(postResp.uuid, delResp.uuid);

		// delete an invalid booking
		endpoint = "/spot/booking/" + UUID.randomUUID().toString();
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Booking not found.",
				null, HttpMethod.DELETE, endpoint, null);
		
		// get customer's active bookings
		var responses = this
				.exchange(HttpMethod.GET, "/customer/spot/booking", ArrayList.class, HttpStatus.OK)
				.getBody();
		int i = 0;
		for (@SuppressWarnings("unused")
		var obj : responses) {
			++i;
		}
		assertEquals(0, i);
	}

}
