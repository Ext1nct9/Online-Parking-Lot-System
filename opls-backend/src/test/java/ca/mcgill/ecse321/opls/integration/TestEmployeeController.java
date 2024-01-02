package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;
import java.util.ArrayList;
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
import ca.mcgill.ecse321.opls.dto.EmployeeDto;
import ca.mcgill.ecse321.opls.dto.ScheduleDto;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import ca.mcgill.ecse321.opls.service.EmployeeService;
import ca.mcgill.ecse321.opls.service.UserAccountService;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestEmployeeController extends OplsApiTester{

    @Autowired
    private OplsStartupService startupService;

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private UserAccountService userService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountClaimRepository userAccountClaimRepository;

    private static final String jobTitle = "janitor";

    private static final Double salary = 4.20;

    private Employee employee;

    @Override
    @PostConstruct
    public void specifyAuthentication() {
        this.addDefaultClaim(OAuthClaim.ADMIN);
        this.addDefaultClaim(OAuthClaim.EMPLOYEE);
    }

    @Override
    @BeforeEach
    public void setupTestData() {
        employee = employeeService.createEmployee(this.getUserAccount().getUuid(), jobTitle, salary);
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

    /**
     * Test endpoint GET /employee/{uuid}
     */
    @Test
    public void testGetEmployee() {
        // test valid request
        EmployeeDto response = this.exchange(HttpMethod.GET, MessageFormat.format(
                "/employee/{0}", employee.getUuid().toString()), EmployeeDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(employee.getUserAccount().getUuid(), response.userAccountUUID);
        assertEquals(employee.getJobTitle(), response.jobTitle);
        assertEquals(employee.getSalary(), response.salary);

        // test invalid request with invalid UUID
        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.GET, MessageFormat.format(
                "/employee/{0}", UUID.fromString("00000000-0000-0000-0000-000000000000")), null);
        
		// test get all employees
		var employees = this.exchange(HttpMethod.POST, "/employee/search",
				ArrayList.class, HttpStatus.OK).getBody();
		assertEquals(1, employees.size());
		ObjectMapper objectMapper = new ObjectMapper();
		for (var obj : employees) {
			var emp = objectMapper.convertValue(obj, EmployeeDto.class);
	        assertEquals(employee.getUserAccount().getUuid(), emp.userAccountUUID);
	        assertEquals(employee.getJobTitle(), emp.jobTitle);
	        assertEquals(employee.getSalary(), emp.salary);
		}
    }

    /**
     * Test endpoint GET /employee
     */
    @Test
    public void testGetEmployeeSelf() {
        // test valid request
        EmployeeDto response = this.exchange(HttpMethod.GET, "/employee", EmployeeDto.class, HttpStatus.OK).
                getBody();

        var userAccount = this.getUserAccount();
        
        assertNotNull(response);
        assertEquals(userAccount.getUuid(), response.userAccountUUID);
        assertEquals(userAccount.getUsername(), userService.getUserAccount(response.userAccountUUID).getUsername());
        assertEquals(employee.getJobTitle(), response.jobTitle);
        assertEquals(employee.getSalary(), response.salary);

        // we do not test for an invalid request because the user account ID in the access token will always be valid
    }

    /**
     * Test endpoint POST /employee
     */
    @Test
    public void testCreateEmployee() {
    	var userUuid = this.getUserAccount().getUuid();
    	
    	// create existing employee
    	employeeService.deleteEmployee(employee.getUuid());
    	
        // test valid request
        EmployeeDto request = new EmployeeDto(jobTitle, salary, userUuid,employee.getUuid());
        EmployeeDto response = this.exchange(HttpMethod.POST, "/employee", request, EmployeeDto.class,
                HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(userUuid, response.userAccountUUID);
        assertEquals(jobTitle, response.jobTitle);
        assertEquals(salary, response.salary);
        assertTrue(userAccountClaimRepository.userHasClaim(
                userAccountRepository.findUserAccountByUuid(response.userAccountUUID), OAuthClaim.EMPLOYEE));

        // test invalid requests
        request = new EmployeeDto(jobTitle, salary, UUID.fromString("00000000-0000-0000-0000-000000000000"), employee.getUuid());

        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the user account with the given UUID does not exist!", HttpMethod.POST,
                "/employee", request, null);

        request = new EmployeeDto("", -1234567.89, userUuid, employee.getUuid());

        var error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.POST, "/employee", request,
                null);

        assertTrue(error.hasField("jobTitle", "The field must be at least 1 character!"));
        assertTrue(error.hasField("salary", "The salary must be a positive number greater than 0.0!"));
    }

    /**
     * Test endpoint PUT /employee/{uuid}
     */
    @Test
    public void testUpdateEmployee() {
        // test valid request
        EmployeeDto request = new EmployeeDto("ceo", 1234567.89, employee.getUserAccount().getUuid(),employee.getUuid());

        EmployeeDto response = this.exchange(HttpMethod.PUT, MessageFormat.format("/employee/{0}",
                employee.getUuid().toString()), request, EmployeeDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals("ceo", response.jobTitle);
        assertEquals(1234567.89, response.salary);

        // test invalid request
        request = new EmployeeDto("", -1234567.89, employee.getUserAccount().getUuid(),employee.getUuid());

        var error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.PUT, MessageFormat.format(
                        "/employee/{0}", employee.getUuid().toString()), request, null);

        assertTrue(error.hasField("jobTitle", "The field must be at least 1 character!"));
        assertTrue(error.hasField("salary", "The salary must be a positive number greater than 0.0!"));
    }

    /**
     * Test endpoint DELETE /employee/{uuid}
     */
    @Test
    public void testDeleteEmployee() {
        // test valid request
        this.exchange(HttpMethod.DELETE, MessageFormat.format("/employee/{0}",
                employee.getUuid().toString()), EmployeeDto.class, HttpStatus.OK);

        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.GET, MessageFormat.format(
                        "/employee/{0}", employee.getUuid().toString()), null);

        // test invalid request
        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.DELETE, MessageFormat.format(
                        "/employee/{0}", employee.getUuid().toString()), null);
    }
}
