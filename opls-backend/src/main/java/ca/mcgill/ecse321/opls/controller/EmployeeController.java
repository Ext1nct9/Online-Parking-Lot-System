package ca.mcgill.ecse321.opls.controller;

import java.util.Collections;
import java.util.UUID;
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
import ca.mcgill.ecse321.opls.dto.EmployeeDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Employee operations.
 */
@RestController
@CrossOrigin
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;
    
    /**
     * Search for employees.
     * 
     * @HTTPMethod			POST
     * @URL					/employee/search
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @return				A list of active employees.
     */
    @PostMapping(value = "/employee/search")
    @ResponseBody
    public Iterable<EmployeeDto> getEmployees(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
    	AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));
    	
    	var results = employeeService.getEmployees();
    	return StreamSupport.stream(results.spliterator(), false)
    			.map((e) -> new EmployeeDto(e))
    			.collect(Collectors.toList());
    }

    /**
     * Retrieve the employee with the given UUID.
     * 
     * @HTTPmethod         	GET
     * @URL                 /employee/{uuid}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee.
     * @return				The retrieved employee.
     */
    @GetMapping(value = "/employee/{uuid}")
    @ResponseBody
    public EmployeeDto getEmployee(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new EmployeeDto(employeeService.getEmployee(uuid));
    }

    /**
     * Retrieve the employee profile of the current session. 
     * 
     * @HTTPmethod          GET
     * @URL                 /employee
     * @param token         Bearer access token. Must have the EMPLOYEE user claim.
     * @return              The retrieved employee.
     */
    @GetMapping(value = "/employee")
    @ResponseBody
    public EmployeeDto getEmployeeSelf(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
        var credentials = AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.EMPLOYEE));

        return new EmployeeDto(employeeService.getEmployee(credentials.userAccountId));
    }

    /**
     * Creates an employee with the given job title and salary, and an associated user account.
     * 
     * @HTTPmethod          POST
     * @URL                 /employee
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param request       The employee fields to create.
     * @return              The created employee.
     */
    @PostMapping(value = "/employee")
    @ResponseBody
    public EmployeeDto createEmployee(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @Valid @RequestBody EmployeeDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new EmployeeDto(employeeService.
                createEmployee(request.userAccountUUID, request.jobTitle, request.salary));
    }

    /**
     * Updates the employee with the given UUID with the given job title and salary.
     * 
     * @HTTPmethod          PUT
     * @URL                 /employee/{uuid}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee to update.
     * @param request       The fields to update.
     * @return              The updated employee object.
     */
    @PutMapping(value = "/employee/{uuid}")
    @ResponseBody
    public EmployeeDto updateEmployee(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid,
            @Valid @RequestBody EmployeeDto request) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new EmployeeDto(employeeService.updateEmployee(uuid, request.jobTitle, request.salary));
    }

    /**
     * Deletes the employee with the given UUID.
     * 
     * @HTTPmethod          DELETE
     * @URL                 /employee/{uuid}
     * @param token         Bearer access token. Must have the ADMIN user claim.
     * @param uuid          UUID of the employee to delete.
     * @return              The deleted employee object.
     */
    @DeleteMapping(value = "/employee/{uuid}")
    @ResponseBody
    public EmployeeDto deleteEmployee(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @NotNull @PathVariable("uuid") UUID uuid) {
        AccessTokenHelper.parseAccessToken(
                token, true, Collections.singleton(OAuthClaim.ADMIN));

        return new EmployeeDto(employeeService.deleteEmployee(uuid));
    }

}
