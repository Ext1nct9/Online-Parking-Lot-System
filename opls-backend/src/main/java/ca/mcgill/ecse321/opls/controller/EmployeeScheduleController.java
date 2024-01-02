package ca.mcgill.ecse321.opls.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
import ca.mcgill.ecse321.opls.model.Schedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.EmployeeScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Employee schedule operations.
 */
@RestController
@CrossOrigin
public class EmployeeScheduleController {

    @Autowired
    EmployeeScheduleService employeeScheduleService;

    /**
     * Retrieves the schedule of an employee.
     * 
     * @HTTPmethod          GET
     * @URL                 /employee/{uuid}/schedule
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee.
     * @return              The employee's schedule entries.
     */
    @GetMapping(value = "/employee/{uuid}/schedule")
    @ResponseBody
    public List<ScheduleDto> getEmployeeSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return employeeScheduleService.getEmployeeAllSchedules(uuid).stream()
                .map(ScheduleDto::new).collect(Collectors.toList());
    }

    /**
     * Retrieves the employee schedule of the current user.
     * 
     * @HTTPmethod          GET
     * @URL                 /employee/schedule
     * @param token         Bearer access token. Must have the EMPLOYEE user claim.
     * @return              The employee's schedule entries.
     */
    @GetMapping(value = "/employee/schedule")
    @ResponseBody
    public List<ScheduleDto> getEmployeeOwnSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
        var auth = AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.EMPLOYEE));

        return employeeScheduleService.getEmployeeAllSchedules(auth.userAccountId).stream()
                .map(ScheduleDto::new).collect(Collectors.toList());
    }

    /**
     * Creates a schedule for an employee.
     * 
     * @HTTPmethod          POST
     * @URL                 /employee/{uuid}/schedule/{day}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee.
     * @param day			The day of the week for the schedule.
     * @param request       The schedule information.
     * @return              The created schedule.
     */
    @PostMapping(value = "/employee/{uuid}/schedule/{day}")
    @ResponseBody
    public ScheduleDto createEmployeeSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid,
            @NotNull @PathVariable("day") String day,
            @Valid @RequestBody ScheduleDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new ScheduleDto(employeeScheduleService.createEmployeeSchedule(
                uuid, Day.valueOf(day.toUpperCase()), request.startTime, request.endTime));
    }

    /**
     * Update a schedule for an employee.
     * 
     * @HTTPmethod          PUT
     * @URL                 /employee/{uuid}/schedule/{day}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee.
     * @param day			The day of the week for the schedule.
     * @param request       The schedule information.
     * @return              The updated schedule.
     */
    @PutMapping(value = "/employee/{uuid}/schedule/{day}")
    @ResponseBody
    public ScheduleDto updateEmployeeSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid,
            @NotNull @PathVariable("day") String day,
            @Valid @RequestBody ScheduleDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new ScheduleDto(employeeScheduleService.updateEmployeeSchedule(
                uuid, Schedule.Day.valueOf(day.toUpperCase()), request.startTime, request.endTime));
    }

    /**
     * Delete a schedule for an employee.
     * 
     * @HTTPmethod          DELETE
     * @URL                 /employee/{uuid}/schedule/{day}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee.
     * @param day			The day of the week for the schedule.
     * @return              The deleted schedule.
     */
    @DeleteMapping(value = "/employee/{uuid}/schedule/{day}")
    @ResponseBody
    public ScheduleDto deleteEmployeeSchedule(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid,
            @NotNull @PathVariable("day") String day) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new ScheduleDto(employeeScheduleService.deleteEmployeeSchedule(
                uuid, Day.valueOf(day.toUpperCase())));
    }

}
