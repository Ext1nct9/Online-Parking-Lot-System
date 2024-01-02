package ca.mcgill.ecse321.opls.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotDto;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotQueryRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotQueryResponseDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.ParkingSpotService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Operations related to parking spots.
 */
@RestController
@CrossOrigin
public class ParkingSpotController {

	@Autowired
	private ParkingSpotService parkingSpotService;

	/**
	 * Create a parking spot.
	 * 
	 * @HTTPMethod			POST
	 * @URL					/spot
	 * @param token			Bearer access token. Must have the ADMIN user claim.
	 * @param request		The parking spot fields.
	 * @return 				The created parking spot entity.
	 */
	@PostMapping(value = "/spot")
	@ResponseBody
	public ParkingSpotDto createSpot(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody ParkingSpotDto request) {
		AccessTokenHelper.parseAccessToken(token, true,
				Arrays.asList(OAuthClaim.ADMIN));

		return new ParkingSpotDto(
				parkingSpotService.createParkingSpot(request.toModel()));
	}

	/**
	 * Update a parking spot.
	 * 
	 * @HTTPMethod			PUT
	 * @URL					/spot/{id}
	 * @param token 		Bearer access token. Must have the ADMIN or EMPLOYEE user claims.
	 * @param id 			The spot id to update.
	 * @return				The updated parking spot entity.
	 */
	@PutMapping(value = "/spot/{id}")
	@ResponseBody
	public ParkingSpotDto updateParkingSpot(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("id") String id,
			@Valid @RequestBody ParkingSpotDto request) {
		AccessTokenHelper.parseAccessToken(token, true,
				Arrays.asList(OAuthClaim.ADMIN, OAuthClaim.EMPLOYEE));
		return new ParkingSpotDto(
				parkingSpotService.updateParkingSpot(id, request));
	}
	
	/**
	 * Get a parking spot.
	 * 
	 * @HTTPMethod			GET
	 * @URL					/spot/{id}
	 * @param token 		Bearer access token. No required registration or user claims.
	 * @param id 			The spot id to fetch.
	 * @return				The retrieved parking spot entity.
	 */
	@GetMapping(value = "/spot/{id}")
	@ResponseBody
	public ParkingSpotDto getParkingSpot(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("id") String id) {
		AccessTokenHelper.parseAccessToken(token, false, null);
		return new ParkingSpotDto(parkingSpotService.getParkingSpot(id));
	}
	
	/**
	 * Delete a parking spot
	 * 
	 * @HTTPMethod			DELETE
	 * @URL					/spot/{id}
	 * @param token			Bearer access token. Must have the ADMIN user claim.
	 * @param id 			The id of the parking spot to delete.
	 * @return				The deleted entity.
	 */
	@DeleteMapping(value = "/spot/{id}")
	@ResponseBody
	public ParkingSpotDto deleteParkingSpot(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("id") String id) {
		AccessTokenHelper.parseAccessToken(token, true,
				Collections.singleton(OAuthClaim.ADMIN));
		return new ParkingSpotDto(parkingSpotService.deleteParkingSpot(id));
	}
	
	/**
	 * Query for parking spots with filters.
	 * 
	 * @HTTPMethod			POST
	 * @URL					/spot/search
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param queryReq		The query request.
	 * @return				Search results.
	 */
	@PostMapping(value="/spot/search")
	@ResponseBody
	public ParkingSpotQueryResponseDto exactQueryParkingSpots(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody ParkingSpotQueryRequestDto queryReq) {
		AccessTokenHelper.parseAccessToken(token, false, null);

		// query
		var results = parkingSpotService.query(queryReq);

		// convert
		return new ParkingSpotQueryResponseDto(
				StreamSupport.stream(results.spliterator(), false)
						.map((ps) -> new ParkingSpotDto(ps))
						.collect(Collectors.toList()));
	}
	
	/**
	 * Query count of parking spots with filters.
	 * 
	 * @HTTPMethod			POST
	 * @URL					/spot/search/count
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param queryReq		The query request.
	 * @return				Search results.
	 */
	@PostMapping(value="/spot/search/count")
	@ResponseBody
	public ParkingSpotQueryResponseDto exactQueryParkingSpotCount(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody ParkingSpotQueryRequestDto queryReq) {
		AccessTokenHelper.parseAccessToken(token, false, null);

		// query
		var count = parkingSpotService.queryCount(queryReq);
		
		// return count
		var response = new ParkingSpotQueryResponseDto();
		response.count = count;
		return response;
	}

}
