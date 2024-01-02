package ca.mcgill.ecse321.opls.controller;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import ca.mcgill.ecse321.opls.dto.DateRangeDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingQueryRequestDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingQueryResponseDto;
import ca.mcgill.ecse321.opls.dto.service.booking.ServiceBookingRequestDto;
import ca.mcgill.ecse321.opls.dto.service.booking.UpdateServiceBookingRequestDto;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.PaymentService;
import ca.mcgill.ecse321.opls.service.VehicleServiceBookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Vehicle service booking operations.
 */
@RestController
@CrossOrigin
public class VehicleServiceBookingController {

	@Autowired
	private VehicleServiceBookingService vehicleServiceBookingService;

	@Autowired
	private PaymentService paymentService;

	/**
	 * Request a vehicle service.
	 * 
	 * @HTTPMethod 			POST
	 * @URL 				/service/{serviceId}/booking
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param request 		Booking request.
	 * @return				The created booking.
	 */
	@PostMapping(value = "/service/{serviceId}/booking")
	@ResponseBody
	public ServiceBookingDto requestBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("serviceId") String serviceId,
			@Valid @RequestBody ServiceBookingRequestDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, false,
				null);

		// Check if user is registered
		Integer userAccountId = credentials.getCustomerUserId();
		// Validate request with service
		var booking = vehicleServiceBookingService.processBooking(serviceId,
				request.startDate);
		// Payment
		paymentService.submitPayment(credentials.hasClaim(OAuthClaim.EMPLOYEE),
				request.creditCardNumber, booking.getCost());
		booking.setBookingStatus(BookingStatus.PAID);
		// Save booking
		booking = vehicleServiceBookingService.saveBooking(booking,
				userAccountId, request.licensePlate);

		return new ServiceBookingDto(booking);
	}

	/**
	 * Get a vehicle service booking by its ID.
	 * 
	 * @HTTPMethod 			GET
	 * @URL 				/service/booking/{uuid}
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE claims.
	 * @param uuid 			The vehicle service booking ID.
	 * @return 				The vehicle service booking with the ID.
	 */
	@GetMapping(value = "/service/booking/{uuid}")
	@ResponseBody
	public ServiceBookingDto getBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("uuid") UUID uuid) {
		AccessTokenHelper.parseAccessToken(token, false,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));
		var vsb = vehicleServiceBookingService.getVehicleServiceBooking(uuid);
		return new ServiceBookingDto(vsb);
	}

	/**
	 * Get a vehicle service booking by its confirmation number.
	 * 
	 * @HTTPMethod					GET
	 * @URL 						/service/booking/byConfirmation/{confirmationNumber}
	 * @param token					Bearer access token. No required registration or user claims.
	 * @param confirmationNumber	The vehicle service booking confirmation number.
	 * @return 						The vehicle service booking with the confirmation number.
	 */
	@GetMapping(value = "/service/booking/byConfirmation/{confirmationNumber}")
	@ResponseBody
	public ServiceBookingDto getBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("confirmationNumber") String confirmationNumber) {
		AccessTokenHelper.parseAccessToken(token, false, null);
		var vsb = vehicleServiceBookingService
				.getVehicleServiceBooking(confirmationNumber.toUpperCase());
		return new ServiceBookingDto(vsb);
	}

	/**
	 * Updates the booking with the given UUID.
	 * 
	 * @HTTPmethod 			PATCH
	 * @URL 				/service/booking/{uuid}
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE claims.
	 * @param uuid			UUID of the target booking.
	 * @param request		Updated booking fields.
	 * @return 				The updated booking.
	 */
	@PatchMapping(value = "/service/booking/{uuid}")
	@ResponseBody
	public ServiceBookingDto updateVehicleServiceBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("uuid") UUID uuid,
			@Valid @RequestBody UpdateServiceBookingRequestDto request) {
		AccessTokenHelper.parseAccessToken(token, true,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));
		var vsb = vehicleServiceBookingService.updateVehicleServiceBooking(uuid,
				request.licensePlate, request.bookingStatus);
		return new ServiceBookingDto(vsb);
	}

	/**
	 * Deletes the vehicle service booking with the given id.
	 * 
	 * @HTTPmethod 			DELETE
	 * @URL 				/service/booking/{uuid}
	 * @param token			Bearer access token. Must have the ADMIN or EMPLOYEE claims.
	 * @param id			Id of the booking being deleted
	 * @return				The booking that was deleted.
	 */
	@DeleteMapping(value = "/service/booking/{uuid}")
	public ServiceBookingDto deleteVehicleServiceBooking(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("uuid") UUID uuid) {
		AccessTokenHelper.parseAccessToken(token, false,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));

		return new ServiceBookingDto(vehicleServiceBookingService.deleteVehicleServiceBooking(uuid));
	}
	
	/**
	 * Search for existing bookings.
	 * 
	 * @HTTPMethod			POST
	 * @URL					/service/booking/search
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param request		The query request.
	 * @return 				The queried results.
	 */
	@PostMapping(value = "/service/booking/search")
	public ServiceBookingQueryResponseDto queryBookings(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody ServiceBookingQueryRequestDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, false,
				null);

		Integer customerUserId = request.queryOwn
				? credentials.getCustomerUserId()
				: null;

		// query
		var results = vehicleServiceBookingService.queryBookings(customerUserId,
				request.vehicleServiceIds, request.startDate, request.endDate);

		// convert
		return new ServiceBookingQueryResponseDto(StreamSupport.stream(results.spliterator(), false)
				.map((dr) -> new DateRangeDto(dr))
				.collect(Collectors.toList()));
	}
	
}
