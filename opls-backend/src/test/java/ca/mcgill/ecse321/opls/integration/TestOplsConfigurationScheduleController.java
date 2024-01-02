package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
import java.text.MessageFormat;
import java.util.ArrayList;

import ca.mcgill.ecse321.opls.dto.ScheduleDto;
import ca.mcgill.ecse321.opls.model.Schedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.service.OplsConfigurationScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;


import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestOplsConfigurationScheduleController extends OplsApiTester {

    @Autowired
    private OplsStartupService startupService;

    @Autowired
    private OplsConfigurationScheduleService oplsConfigurationScheduleService;

    private static final Day day1 = Day.MONDAY, day2 = Day.FRIDAY, day3 = Day.SATURDAY;

    private static final Time startTime1 = Time.valueOf("10:10:10"), endTime1 = Time.valueOf("10:20:20"),
            startTime2 = Time.valueOf("20:20:20"), endTime2 = Time.valueOf("20:20:30");

    private ArrayList<Day> days = new ArrayList<>();

    private ArrayList<Time> times = new ArrayList<>();

    @Override
    @PostConstruct
    public void specifyAuthentication() {
        this.addDefaultClaim(OAuthClaim.ADMIN);
    }

    @Override
    @BeforeEach
    public void setupTestData() {
        days.add(day1);
        days.add(day2);
        times.add(startTime1);
        times.add(startTime2);
        times.add(endTime1);
        times.add(endTime2);
    }

    @AfterEach
    public void clearDatabase() throws Exception {
        startupService.startupTest();
    }

    @BeforeEach
    public void setupData() {
        startupService.initializeConfiguration();
        startupService.initializeParkingLotStructure();

        for (Day day: Schedule.Day.values()) {
            oplsConfigurationScheduleService.deleteParkingLotSystemSchedule(day);
        }

        oplsConfigurationScheduleService.createParkingLotSystemSchedule(day1, startTime1, endTime1);
        oplsConfigurationScheduleService.createParkingLotSystemSchedule(day2, startTime2, endTime2);
    }

    /**
     * Test endpoint GET /config/schedule
     */
    @Test
    public void testGetConfigurationSchedules() {
        // test with valid request
        var response = this.exchange(HttpMethod.GET, "/config/schedule",
                ArrayList.class, HttpStatus.OK).getBody();

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
     * Test endpoint POST /config/schedule/{day}
     */
    @Test
    public void testCreateConfigurationSchedule() {
        // test with valid request
        ScheduleDto request = new ScheduleDto(day3, startTime1, endTime1);

        ScheduleDto response = this.exchange(HttpMethod.POST, MessageFormat.format(
                "/config/schedule/{0}", day3.toString()), request, ScheduleDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(3, oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().size());
        assertEquals(day3, response.day);
        assertEquals(startTime1, response.startTime);
        assertEquals(endTime1, response.endTime);

        // test with invalid requests
        oplsConfigurationScheduleService.deleteParkingLotSystemSchedule(day3);
        request = new ScheduleDto(day1, startTime1, endTime1);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "a parking lot system configuration schedule for this day already exists!",
                HttpMethod.POST, MessageFormat.format("/config/schedule/{0}", day1.toString()), request,
                null);

        request = new ScheduleDto(day3, endTime1, startTime1);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "the start time is after the end time!", HttpMethod.POST,
                MessageFormat.format("/config/schedule/{0}", day3.toString()), request, null);
    }

    /**
     * Test endpoint PUT /config/schedule/{day}
     */
    @Test
    public void testUpdateConfigurationSchedule() {
        // test with valid request
        ScheduleDto request = new ScheduleDto(day1, startTime2, endTime2);

        ScheduleDto response = this.exchange(HttpMethod.PUT, MessageFormat.format(
                "/config/schedule/{0}", day1.toString()), request, ScheduleDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(2, oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().size());
        assertEquals(day1, response.day);
        assertEquals(startTime2, response.startTime);
        assertEquals(endTime2, response.endTime);

        request = new ScheduleDto(day3, startTime2, endTime2);

        response = this.exchange(HttpMethod.PUT, MessageFormat.format(
                "/config/schedule/{0}", day3.toString()), request, ScheduleDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(3, oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().size());
        assertEquals(day3, response.day);
        assertEquals(startTime2, response.startTime);
        assertEquals(endTime2, response.endTime);

        // test with invalid request
        request = new ScheduleDto(day1, endTime1, startTime1);

        this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request",
                "the start time is after the end time!", HttpMethod.PUT,
                MessageFormat.format("/config/schedule/{0}", day1.toString()), request, null);
    }

    /**
     * Test endpoint DELETE /config/schedule/{day}
     */
    @Test
    public void testDeleteConfigurationSchedule() {
        // test with valid request
        ScheduleDto response = this.exchange(HttpMethod.DELETE, MessageFormat.format(
                "/config/schedule/{0}", day1.toString()), ScheduleDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(1, oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().size());
        assertEquals(day2, oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().get(0).getDay());
        assertEquals(day1, response.day);

        // test with invalid request
        this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found",
                "a schedule for this day does not exist!", HttpMethod.DELETE,
                MessageFormat.format("/config/schedule/{0}", day3.toString()), null);
    }

}
