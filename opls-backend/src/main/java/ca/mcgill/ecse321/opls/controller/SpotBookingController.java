package ca.mcgill.ecse321.opls.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.dto.spot.booking.IncrementalSpotBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.MonthlySpotBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.PatchSpotBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.booking.SpotBookingResponseDto;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.ParkingSpotBookingService;
import ca.mcgill.ecse321.opls.service.ParkingSpotService;
import ca.mcgill.ecse321.opls.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Parking spot booking operations.
 */
@RestController
@CrossOrigin
public class SpotBookingController {

	@Autowired
	private ParkingSpotService parkingSpotService;

	@Autowired
	private ParkingSpotBookingService parkingSpotBookingService;

	@Autowired
	private PaymentService paymentService;

	/**
	 * Request an incremental parking spot booking.
	 * 
	 * @HTTPMethod			POST
	 * @URL 				/spot/booking/incremental
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param spotRequest	The parking spot booking request.
	 * @return				The created booking.
	 */
	@PostMapping(value = "/spot/booking/incremental")
	@ResponseBody
	public SpotBookingResponseDto requestIncrementalBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody IncrementalSpotBookingRequestDto spotRequest) {
		var credentials = AccessTokenHelper.parseAccessToken(token, false,
				null);

		// determine if user is registered
		Integer userAccountId = credentials.getCustomerUserId();

		// get requested spot
		var spot = parkingSpotService.getParkingSpot(spotRequest.parkingSpotId);

		// validate request with service
		Date now = new Date();
		var booking = parkingSpotBookingService.processIncrementalBooking(spot,
				now, spotRequest.duration, spotRequest.vehicleType);

		// request payment
		paymentService.submitPayment(credentials.hasClaim(OAuthClaim.EMPLOYEE),
				spotRequest.creditCardNumber, booking.getCost());
		booking.setBookingStatus(BookingStatus.PAID);

		// save booking
		booking = parkingSpotBookingService.saveBooking(booking, userAccountId,
				spotRequest.licensePlate);

		// generate response
		return new SpotBookingResponseDto(booking);
	}

	/**
	 * Request a monthly parking spot booking.
	 * 
	 * @HTTPMethod 			POST
	 * @URL 				/spot/booking/monthly
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param spotRequest	The parking spot booking request.
	 * @return 				The created booking.
	 */
	@PostMapping(value = "/spot/booking/monthly")
	@ResponseBody
	public SpotBookingResponseDto requestMonthlyBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody MonthlySpotBookingRequestDto spotRequest) {
		var credentials = AccessTokenHelper.parseAccessToken(token, false,
				null);

		// determine if user is registered
		Integer userAccountId = credentials.getCustomerUserId();

		// validate request with service
		Date now = new Date();
		var booking = parkingSpotBookingService.processMonthlyBooking(now,
				spotRequest.vehicleType);

		// request payment
		paymentService.submitPayment(credentials.hasClaim(OAuthClaim.EMPLOYEE),
				spotRequest.creditCardNumber, booking.getCost());
		booking.setBookingStatus(BookingStatus.PAID);

		// save booking
		booking = parkingSpotBookingService.saveBooking(booking, userAccountId,
				spotRequest.licensePlate);

		// generate response
		return new SpotBookingResponseDto(booking);
	}

	/**
	 * Fetch the current booking for a parking spot.
	 * 
	 * @HTTPMethod 			GET
	 * @URL 				/spot/{id}/booking
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE user claim.
	 * @param id			The parking spot ID.
	 * @return 				The current booking for the parking spot, null if it does not exist.
	 */
	@GetMapping(value = "/spot/{id}/booking")
	@ResponseBody
	public ResponseEntity<SpotBookingResponseDto> getCurrentBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("id") String id) {
		AccessTokenHelper.parseAccessToken(token, false,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));

		var spot = parkingSpotService.getParkingSpot(id);

		var booking = parkingSpotBookingService.getBooking(spot);

		ResponseEntity<SpotBookingResponseDto> response;
		if (booking == null) {
			response = new ResponseEntity<SpotBookingResponseDto>(
					new SpotBookingResponseDto(), HttpStatus.NOT_FOUND);
		} else {
			response = new ResponseEntity<SpotBookingResponseDto>(
					new SpotBookingResponseDto(booking), HttpStatus.OK);
		}

		return response;
	}

	/**
	 * Get a parking spot booking by its ID.
	 * 
	 * @HTTPMethod 			GET
	 * @URL 				/spot/booking/{uuid}
	 * @param token			Bearer access token. Must have the ADMIN, EMPLOYEE, or CUSTOMER user claim.
	 * @param uuid			The parking spot booking ID.
	 * @return 				The parking spot booking with the ID.
	 */
	@GetMapping(value = "/spot/booking/{uuid}")
	@ResponseBody
	public SpotBookingResponseDto getBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("uuid") UUID uuid) {
		AccessTokenHelper.parseAccessToken(token, true, Arrays.asList(
				OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE, OAuthClaim.CUSTOMER));

		return new SpotBookingResponseDto(
				parkingSpotBookingService.getBooking(uuid));
	}

	/**
	 * Get a parking spot booking by its confirmation number.
	 * 
	 * @HTTPMethod 					GET
	 * @URL 						/spot/booking/byConfirmation/{confirmationNumber}
	 * @param token					Bearer access token. No required registration or user claims.
	 * @param confirmationNumber	The confirmation number of the booking.
	 * @return 						The parking spot booking with the confirmation number.
	 */
	@GetMapping(value = "/spot/booking/byConfirmation/{confirmationNumber}")
	@ResponseBody
	public SpotBookingResponseDto getBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("confirmationNumber") String confirmationNumber) {
		AccessTokenHelper.parseAccessToken(token, false, null);

		return new SpotBookingResponseDto(parkingSpotBookingService
				.getBooking(confirmationNumber.toUpperCase()));
	}

	/**
	 * Get a list of parking spot bookings by status.
	 * 
	 * @HTTPMethod 			GET
	 * @URL 				/spot/booking/byStatus/{status}
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE user claim.
	 * @param status		The status to filter by.
	 * @return 				A list of parking spot bookings with the status.
	 */
	@GetMapping(value = "/spot/booking/byStatus/{status}")
	@ResponseBody
	public Iterable<SpotBookingResponseDto> getBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("status") BookingStatus status) {
		AccessTokenHelper.parseAccessToken(token, false,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));

		return StreamSupport
				.stream(parkingSpotBookingService.getBookings(status)
						.spliterator(), false)
				.map(b -> new SpotBookingResponseDto(b))
				.collect(Collectors.toList());
	}

	/**
	 * Modify a parking spot booking.
	 * 
	 * @HTTPMethod			PATCH
	 * @URL 				/spot/booking/{uuid}
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE user claims.
	 * @param uuid			The parking spot booking ID.
	 * @param request		The parking spot booking request.
	 * @return 				The updated parking spot booking.
	 */
	@PatchMapping(value = "/spot/booking/{uuid}")
	@ResponseBody
	public SpotBookingResponseDto patchBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@NotNull @PathVariable("uuid") UUID uuid,
			@Valid @RequestBody PatchSpotBookingRequestDto request) {
		AccessTokenHelper.parseAccessToken(authorization, true,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));

		ParkingSpot spot = null;
		if (request.parkingSpotId != null) {
			spot = parkingSpotService.getParkingSpot(request.parkingSpotId);
		}

		return new SpotBookingResponseDto(parkingSpotBookingService
				.patchBooking(uuid, spot, request.status));
	}

	/**
	 * Delete a parking spot booking.
	 * 
	 * @HTTPMethod 			DELETE
	 * @URL 				/spot/booking/{uuid}
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE user claims.
	 * @param uuid			The parking spot booking ID.
	 * @return 				The deleted parking spot booking.
	 */
	@DeleteMapping(value = "/spot/booking/{uuid}")
	@ResponseBody
	public SpotBookingResponseDto deleteBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("uuid") UUID uuid) {
		AccessTokenHelper.parseAccessToken(token, true,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));

		return new SpotBookingResponseDto(
				parkingSpotBookingService.deleteBooking(uuid));
	}
	
	/**
	 * Get a customer's active parking spot bookings.
	 * 
	 * @HTTPMethod			GET
	 * @URL					/customer/spot/booking
	 * @param token			Bearer access token. Must have the CUSTOMER user claim.
	 * @return				A list of parking spot bookings.
	 */
	@GetMapping(value = "/customer/spot/booking")
	@ResponseBody
	public Iterable<SpotBookingResponseDto> getCustomerBookings(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true,
				Arrays.asList(OAuthClaim.CUSTOMER));
		
		var results = parkingSpotBookingService.getCustomerBookings(credentials.getCustomerUserId());
		
		return StreamSupport.stream(results.spliterator(), false)
    			.map((psb) -> new SpotBookingResponseDto(psb))
    			.collect(Collectors.toList());
	}

}
