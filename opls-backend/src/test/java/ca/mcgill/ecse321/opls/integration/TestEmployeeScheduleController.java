package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
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
import ca.mcgill.ecse321.opls.dto.ScheduleDto;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import ca.mcgill.ecse321.opls.service.EmployeeScheduleService;
import ca.mcgill.ecse321.opls.service.EmployeeService;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestEmployeeScheduleController extends OplsApiTester {

    @Autowired
    private OplsStartupService startupService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeScheduleService employeeScheduleService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private static final String jobTitle = "janitor", username = "krustykrab", password = "spongebobsquarepants",
            firstName = "spongebob", lastName = "squarepants", securityQuestion = "where do you work?",
            securityQuestionAnswer = "the krusty krab";

    private static final Double salary = 4.20;

    private static final Day day1 = Day.MONDAY, day2 = Day.FRIDAY, day3 = Day.SATURDAY;

    private static final Time startTime1 = Time.valueOf("10:10:10"), endTime1 = Time.valueOf("10:20:20"),
            startTime2 = Time.valueOf("20:20:20"), endTime2 = Time.valueOf("20:20:30");

    private ArrayList<Day> days = new ArrayList<>();

    private ArrayList<Time> times = new ArrayList<>();

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

        employeeScheduleService.createEmployeeSchedule(employee.getUuid(), day1, startTime1, endTime1);
        employeeScheduleService.createEmployeeSchedule(employee.getUuid(), day2, startTime2, endTime2);

        days.add(day1);
        days.add(day2);
        times.add(startTime1);
        times.add(startTime2);
        times.add(endTime1);
        times.add(endTime2);

        ObjectMapper objectMapper = new ObjectMapper();
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
     * Test endpoint GET /employee/{uuid}/schedule
     */
    @Test
    public void testGetEmployeeSchedule() {
        // test valid request
        var response = this.exchange(HttpMethod.GET, MessageFormat.format("/employee/{0}/schedule",
                        employee.getUuid()), ArrayList.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(2, response.size());
        ObjectMapper objectMapper = new ObjectMapper();
        for (var obj: response) {
            ScheduleDto schedule = objectMapper.convertValue(obj, ScheduleDto.class);
            assertTrue(days.contains(schedule.day));
            assertTrue(times.contains(schedule.startTime));
            assertTrue(times.contains(schedule.endTime));
        }

        // test invalid request
        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.GET, MessageFormat.format(
                        "/employee/{0}/schedule", UUID.fromString("00000000-0000-0000-0000-000000000000")),
                null);
    }

    /**
     * Test endpoint GET /employee/schedule
     */
    @Test
    public void testGetEmployeeOwnSchedule() {
        // test valid request
        var response = this.exchange(HttpMethod.GET, "/employee/schedule", ArrayList.class,
                HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(2, response.size());
        ObjectMapper objectMapper = new ObjectMapper();
        for (var obj: response) {
            ScheduleDto schedule = objectMapper.convertValue(obj, ScheduleDto.class);
            assertTrue(days.contains(schedule.day));
            assertTrue(times.contains(schedule.startTime));
            assertTrue(times.contains(schedule.endTime));
        }

        // there are no invalid request cases
    }

    /**
     * Test endpoint POST /employee/{uuid}/schedule/{day}
     */
    @Test
    public void testCreateEmployeeSchedule() {
        // test valid request
        ScheduleDto request = new ScheduleDto(day3, startTime1, endTime1);

        ScheduleDto response = this.exchange(HttpMethod.POST, MessageFormat.format(
                "/employee/{0}/schedule/{1}", employee.getUuid(), day3.toString()), request, ScheduleDto.class,
                HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(3, employeeScheduleService.getEmployeeAllSchedules(employee.getUuid()).size());
        assertEquals(day3, response.day);
        assertEquals(startTime1, response.startTime);
        assertEquals(endTime1, response.endTime);

        // test invalid requests
        employeeScheduleService.deleteEmployeeSchedule(employee.getUuid(), day3);

        request = new ScheduleDto(day3, endTime1, startTime1);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "the start time is after the end time!", HttpMethod.POST,
                MessageFormat.format("/employee/{0}/schedule/{1}", employee.getUuid(), day3.toString()),
                request, null);

        request = new ScheduleDto(day3, startTime1, endTime1);

        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.POST,
                MessageFormat.format("/employee/{0}/schedule/{1}",
                        UUID.fromString("00000000-0000-0000-0000-000000000000"), day3.toString()),
                request, null);

        request = new ScheduleDto(day1, startTime1, endTime1);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "an employee schedule for this day already exists!",
                HttpMethod.POST, MessageFormat.format("/employee/{0}/schedule/{1}", employee.getUuid(),
                        day1.toString()), request, null);
    }

    /**
     * Test endpoint PUT /employee/{uuid}/schedule/{day}
     */
    @Test
    public void testUpdateEmployeeSchedule() {
        // test with valid request
        ScheduleDto request = new ScheduleDto(day1, startTime2, endTime2);

        ScheduleDto response = this.exchange(HttpMethod.PUT, MessageFormat.format("/employee/{0}/schedule/{1}",
                employee.getUuid(), day1.toString()), request, ScheduleDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(2, employeeScheduleService.getEmployeeAllSchedules(employee.getUuid()).size());
        assertEquals(day1, response.day);
        assertEquals(startTime2, response.startTime);
        assertEquals(endTime2, response.endTime);

        request = new ScheduleDto(day3, startTime2, endTime2);

        response = this.exchange(HttpMethod.PUT, MessageFormat.format("/employee/{0}/schedule/{1}",
                employee.getUuid(), day3.toString()), request, ScheduleDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(3, employeeScheduleService.getEmployeeAllSchedules(employee.getUuid()).size());
        assertEquals(day3, response.day);
        assertEquals(startTime2, response.startTime);
        assertEquals(endTime2, response.endTime);

        // test with invalid requests
        request = new ScheduleDto(day1, endTime1, startTime1);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "the start time is after the end time!", HttpMethod.PUT,
                MessageFormat.format("/employee/{0}/schedule/{1}", employee.getUuid(), day1.toString()),
                request, null);

        request = new ScheduleDto(day1, startTime1, endTime1);

        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.PUT,
                MessageFormat.format("/employee/{0}/schedule/{1}",
                        UUID.fromString("00000000-0000-0000-0000-000000000000"), day1.toString()),
                request, null);
    }

    /**
     * Test endpoint DELETE /employee/{uuid}/schedule/{day}
     */
    @Test
    public void testDeleteEmployeeSchedule() {
        // test with valid request
        ScheduleDto response = this.exchange(HttpMethod.DELETE, MessageFormat.format(
                "/employee/{0}/schedule/{1}", employee.getUuid(), day1.toString()), ScheduleDto.class,
                HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(1, employeeScheduleService.getEmployeeAllSchedules(employee.getUuid()).size());
        assertEquals(day2, employeeScheduleService.getEmployeeAllSchedules(employee.getUuid()).get(0).getDay());
        assertEquals(day1, response.day);

        // test with invalid requests
        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!", HttpMethod.DELETE,
                MessageFormat.format("/employee/{0}/schedule/{1}",
                        UUID.fromString("00000000-0000-0000-0000-000000000000"), day2.toString()),
                null);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "no schedule for the given employee and day exist!", HttpMethod.DELETE,
                MessageFormat.format("/employee/{0}/schedule/{1}", employee.getUuid(), day3.toString()),
                null);
    }
}

/**
 * ⣿⣿⣿⣿⣿⣿⣿⣿⡿⠿⠛⠛⠛⠋⠉⠈⠉⠉⠉⠉⠛⠻⢿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣿⡿⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⢿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⡏⣀⠀⠀⠀⠀⠀⠀⠀⣀⣤⣤⣤⣄⡀⠀⠀⠀⠀⠀⠀⠀⠙⢿⣿⣿⣿⣿
 * ⣿⣿⣿⢏⣴⣿⣷⠀⠀⠀⠀⠀⢾⣿⣿⣿⣿⣿⣿⡆⠀⠀⠀⠀⠀⠀⠀⠈⣿⣿⣿⣿
 * ⣿⣿⣟⣾⣿⡟⠁⠀⠀⠀⠀⠀⢀⣾⣿⣿⣿⣿⣿⣷⢢⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿
 * ⣿⣿⣿⣿⣟⠀⡴⠄⠀⠀⠀⠀⠀⠀⠙⠻⣿⣿⣿⣿⣷⣄⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿
 * ⣿⣿⣿⠟⠻⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠶⢴⣿⣿⣿⣿⣿⣧⠀⠀⠀⠀⠀⠀⣿⣿⣿
 * ⣿⣁⡀⠀⠀⢰⢠⣦⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⣿⣿⣿⣿⣿⡄⠀⣴⣶⣿⡄⣿⣿⣿
 * ⣿⡋⠀⠀⠀⠎⢸⣿⡆⠀⠀⠀⠀⠀⠀⣴⣿⣿⣿⣿⣿⣿⣿⠗⢘⣿⣟⠛⠿⣼⣿
 * ⣿⣿⠋⢀⡌⢰⣿⡿⢿⡀⠀⠀⠀⠀⠀⠙⠿⣿⣿⣿⣿⣿⡇⠀⢸⣿⣿⣧⢀⣼⣿
 * ⣿⣿⣷⢻⠄⠘⠛⠋⠛⠃⠀⠀⠀⠀⠀⢿⣧⠈⠉⠙⠛⠋⠀⠀⠀⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣧⠀⠈⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠟⠀⠀⠀⠀⢀⢃⠀⠀⢸⣿⣿⣿⣿⣿⣿
 * ⣿⣿⡿⠀⠴⢗⣠⣤⣴⡶⠶⠖⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡸⠀⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⡀⢠⣾⣿⠏⠀⠠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠛⠉⠀⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣧⠈⢹⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⡄⠈⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⣴⣾⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣧⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣿⣦⣄⣀⣀⣀⣀⠀⠀⠀⠀⠘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡄⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣧⠀⠀⠀⠙⣿⣿⡟⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠇⠀⠁⠀⠀⠹⣿⠃⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⣿⣿⣿⣿⡿⠛⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⢐⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
 * ⣿⣿⣿⣿⠿⠛⠉⠉⠁⠀⢻⣿⡇⠀⠀⠀⠀⠀⠀⢀⠈⣿⣿⡿⠉⠛⠛⠛⠉⠉⣿
 * ⣿⡿⠋⠁⠀⠀⢀⣀⣠⡴⣸⣿⣇⡄⠀⠀⠀⠀⢀⡿⠄⠙⠛⠀⣀⣠⣤⣤⠄⠀⣿
 *
 *   ^^ us when our github checks pass
 *
 */
