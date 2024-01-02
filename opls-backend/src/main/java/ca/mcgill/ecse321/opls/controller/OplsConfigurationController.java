package ca.mcgill.ecse321.opls.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.dto.OplsConfigurationDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.OplsConfigurationService;
import jakarta.validation.Valid;

/**
 * Parking lot system configuration operations.
 */
@RestController
@CrossOrigin
public class OplsConfigurationController {

    @Autowired
    OplsConfigurationService oplsConfigurationService;

    /**
     * Retrieves the active parking lot system configuration.
     * 
     * @HTTPmethod          GET
     * @URL                 /config
     * @param token			Bearer access token. No required registration or user claims.
     * @return				The currently active configuration.
     */
    @GetMapping(value = "/config")
    @ResponseBody
    public OplsConfigurationDto getActiveConfiguration(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
    	AccessTokenHelper.parseAccessToken(token, false, null);
    	
        return new OplsConfigurationDto(oplsConfigurationService.getParkingLotSystem());
    }

    /**
     * Updates the active parking lot system configuration.
     * 
     * @HTTPmethod          PUT
     * @URL                 /config
     * @param token			Bearer access token. Must have the ADMIN user claim.
     * @param request       The configuration to update.
     * @return              The updated configuration.
     */
    @PutMapping(value = "/config")
    @ResponseBody
    public OplsConfigurationDto updateActiveConfiguration(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @Valid @RequestBody OplsConfigurationDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new OplsConfigurationDto(oplsConfigurationService.updateParkingLotSystemConfig(
                request.getMonthlyFee(), request.getIncrementFee(), request.incrementTime, request.maxIncrementTime));
    }

}
