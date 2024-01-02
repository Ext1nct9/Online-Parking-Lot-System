package ca.mcgill.ecse321.opls.controller;

import java.util.Collections;
import java.util.List;
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
import ca.mcgill.ecse321.opls.dto.service.VehicleServiceDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.VehicleServiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Vehicle service operations.
 */
@RestController
@CrossOrigin
public class VehicleServiceController {

	@Autowired
	private VehicleServiceService vehicleService;

	/**
     * Retrieves the vehicle service with the given id.
     * 
     * @HTTPmethod         	GET
     * @URL                 /service/{id}
     * @param token			Bearer access token. No required registration or user claims.
     * @param id            Id of the vehicle service.
     * @return              The found vehicle service.
     */
	@GetMapping(value = "/service/{id}")
	@ResponseBody
	public VehicleServiceDto getVehicleService(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("id") String id) {
		AccessTokenHelper.parseAccessToken(token, false, null);
		
		return new VehicleServiceDto(vehicleService.getVehicleService(id));
	}


	/**
     * Creates a vehicle service with the given display name, duration and fee.
     * 
     * @HTTPmethod          POST
     * @URL                 /service
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param request       The vehicle service fields.
     * @return              The created vehicle service.
     */
	@PostMapping(value = "/service")
	@ResponseBody
	public VehicleServiceDto createVehicleService(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody VehicleServiceDto request) {
		AccessTokenHelper.parseAccessToken(token, true,
				Collections.singleton(OAuthClaim.ADMIN));

		return new VehicleServiceDto(vehicleService.createVehicleService(
				request.displayName, request.duration, request.fee));
	}

	 /**
     * Update a vehicle service with the given display name, duration and fee.
     * 
     * @HTTPmethod          PUT
     * @URL                 /service/{id}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param id            Id of the vehicle service to update.
     * @param request       Updated service fields.
     * @return              The updated vehicle service.
     */
    @PutMapping(value = "/service/{id}")
    public VehicleServiceDto updateVehicleService(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("id") String id,
            @Valid @RequestBody VehicleServiceDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new VehicleServiceDto(vehicleService.updateVehicleService(id, request.displayName, request.duration, request.fee));
    }

	/**
     * Delete a vehicle service.
     * 
     * @HTTPmethod          DELETE
     * @URL                 /service/{id}
     * @param token         Bearer access token. Must have the ADMINISTRATOR user claim.
     * @param id            Id of the vehicle service being deleted
     * @return				The deleted vehicle service.
     */
    @DeleteMapping(value = "/service/{id}")
    public VehicleServiceDto deleteVehicleService(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("id") String id) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        VehicleServiceDto vsd = new VehicleServiceDto(vehicleService.getVehicleService(id));
        vehicleService.deleteVehicleService(id);
        return vsd;
        
    }
	
	/**
     * Retrieves all vehicle services
     * 
     * @HTTPmethod          GET
     * @URL                 /service
     * @param token         Bearer access token. No required registration or user claims.
     * @return              A list of all vehicle services.
     */
	@GetMapping(value = "/service")
	public List<VehicleServiceDto> getAllVehicleServices(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
		return StreamSupport
				.stream(vehicleService.getAllVehicleServices()
						.spliterator(), false)
				.map(b -> new VehicleServiceDto(b))
				.collect(Collectors.toList());
	}

}