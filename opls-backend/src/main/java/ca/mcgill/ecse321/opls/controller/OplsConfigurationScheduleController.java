package ca.mcgill.ecse321.opls.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import ca.mcgill.ecse321.opls.dto.ScheduleDto;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.OplsConfigurationScheduleService;
import ca.mcgill.ecse321.opls.service.OplsConfigurationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Parking lot system schedule operations.
 */
@RestController
@CrossOrigin
public class OplsConfigurationScheduleController {

    @Autowired
    OplsConfigurationService oplsConfigurationService;

    @Autowired
    OplsConfigurationScheduleService oplsConfigurationScheduleService;

    /**
     * Retrieves the schedule of the active parking lot system configuration.
     * 
     * @HTTPmethod          GET
     * @URL                 /config/schedule
     * @param token			Bearer access token. No required registration or user claims.
     * @return              A list of schedule elements for the currently active configuration.
     */
    @GetMapping(value = "/config/schedule")
    @ResponseBody
    public List<ScheduleDto> getConfigurationSchedules(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
        return oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().stream()
                .map(ScheduleDto::new).collect(Collectors.toList());
    }

    /**
     * Create a schedule for the active parking lot system configuration.
     * 
     * @HTTPmethod          POST
     * @URL                 /config/schedule/{day}
     * @param token			Bearer access token. Must have the ADMIN user claim.
     * @param day           Day of the week for the new schedule.
     * @param request       The schedule information.
     * @return              The created schedule.
     */
    @PostMapping(value = "/config/schedule/{day}")
    @ResponseBody
    public ScheduleDto createConfigurationSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("day") String day,
            @Valid @RequestBody ScheduleDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new ScheduleDto(oplsConfigurationScheduleService.createParkingLotSystemSchedule(
                Day.valueOf(day.toUpperCase()), request.startTime, request.endTime));
    }

    /**
     * Update a schedule for the active parking lot system configuration.
     * 
     * @HTTPmethod          PUT
     * @URL                 /config/schedule/{day}
     * @param token			Bearer access token. Must have the ADMIN user claim.
     * @param day           Day of the week for the new schedule.
     * @param request       The schedule information.
     * @return              The updated schedule.
     */
    @PutMapping(value = "/config/schedule/{day}")
    @ResponseBody
    public ScheduleDto updateConfigurationSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("day") String day,
            @Valid @RequestBody ScheduleDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new ScheduleDto(oplsConfigurationScheduleService.updateParkingLotSystemSchedule(
                Day.valueOf(day.toUpperCase()), request.startTime, request.endTime));
    }

    /**
     * Delete a schedule for the active parking lot system configuration.
     * 
     * @HTTPmethod          DELETE
     * @URL                 /config/schedule/{day}
     * @param token			Bearer access token. Must have the ADMIN user claim.
     * @param day           Day of the week for the new schedule.
     * @return              The deleted schedule.
     */
    @DeleteMapping(value = "/config/schedule/{day}")
    @ResponseBody
    public ScheduleDto deleteConfigurationSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("day") String day) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new ScheduleDto(oplsConfigurationScheduleService.deleteParkingLotSystemSchedule(
                Day.valueOf(day.toUpperCase())));
    }

}
