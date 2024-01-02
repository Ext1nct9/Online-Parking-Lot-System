package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import ca.mcgill.ecse321.opls.model.ParkingLotSystem.ParkingLotSystemSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class TestOplsConfigurationScheduleService {

    @Mock
    private ParkingLotSystemRepository parkingLotSystemRepository;

    @Mock
    private ParkingLotSystemScheduleRepository parkingLotSystemScheduleRepository;

    @InjectMocks
    private OplsConfigurationScheduleService oplsConfigurationScheduleService;

    private static final Double monthlyFee = 123.45, incrementFee = 678.90;

    private static final int incrementTime = 15, maxIncrementTime = 75;

    private static final Day day1 = Day.MONDAY, day2 = Day.FRIDAY, day3 = Day.SATURDAY;

    private static final Time startTime1 = Time.valueOf("10:10:10"), endTime1 = Time.valueOf("10:20:20"),
            startTime2 = Time.valueOf("20:20:20"), endTime2 = Time.valueOf("20:20:30");

    private ParkingLotSystem pls;

    private ParkingLotSystemSchedule plss1, plss2;

    @BeforeEach
    public void setupMocks() {
        pls = new ParkingLotSystem();
        pls.setMonthlyFee(monthlyFee);
        pls.setIncrementFee(incrementFee);
        pls.setIncrementTime(incrementTime);
        pls.setMaxIncrementTime(maxIncrementTime);

        plss1 = new ParkingLotSystemSchedule();
        plss1.setParkingLotSystem(pls);
        plss1.setDay(day1);
        plss1.setStartTime(startTime1);
        plss1.setEndTime(endTime1);

        plss2 = new ParkingLotSystemSchedule();
        plss2.setParkingLotSystem(pls);
        plss2.setDay(day2);
        plss2.setStartTime(startTime2);
        plss2.setEndTime(endTime2);

        ArrayList<ParkingLotSystemSchedule> schedules = new ArrayList<>();
        schedules.add(plss1);
        schedules.add(plss2);

        lenient().when(parkingLotSystemRepository.getActiveParkingLotSystem()).
                thenAnswer((InvocationOnMock invocation) -> pls);

        lenient().when(parkingLotSystemScheduleRepository.findActiveParkingLotScheduleByDay(any())).
                thenAnswer((InvocationOnMock invocation) -> schedules.stream().filter(schedule ->
                        invocation.getArgument(0).equals(schedule.getDay())).findFirst().orElse(null));

        lenient().when(parkingLotSystemScheduleRepository.findActiveParkingLotSchedules()).
                thenAnswer((InvocationOnMock invocation) -> schedules);

        lenient().when(parkingLotSystemScheduleRepository.save(any())).
                thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

        lenient().doAnswer((InvocationOnMock invocation) -> schedules.remove(invocation.getArgument(0))).
                when(parkingLotSystemScheduleRepository).delete(any());
    }

    @Test
    public void testGetParkingLotSystemSchedule() {
        // test valid request
        ParkingLotSystemSchedule plss = oplsConfigurationScheduleService.getParkingLotSystemSchedule(day1);
        assertNotNull(plss);
        assertEquals(day1, plss.getDay());
        assertEquals(startTime1, plss.getStartTime());
        assertEquals(endTime1, plss.getEndTime());

        // test invalid request
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> oplsConfigurationScheduleService.getParkingLotSystemSchedule(day3));
    }

    @Test
    public void testGetAllParkingLotSystemSchedules() {
        // test valid request
        List<ParkingLotSystemSchedule> schedules = oplsConfigurationScheduleService.getAllParkingLotSystemSchedules();
        assertNotNull(schedules);
        assertEquals(2, schedules.size());
        assertTrue(schedules.contains(plss1));
        assertTrue(schedules.contains(plss2));
    }

    @Test
    public void testCreateParkingLotSystemSchedule() {
        // test valid request
        ParkingLotSystemSchedule plss = oplsConfigurationScheduleService.
                createParkingLotSystemSchedule(day3, startTime1, endTime1);
        assertNotNull(plss);
        assertEquals(day3, plss.getDay());
        assertEquals(startTime1, plss.getStartTime());
        assertEquals(endTime1, plss.getEndTime());

        // test invalid requests
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> oplsConfigurationScheduleService.createParkingLotSystemSchedule(day1, startTime1, endTime1));

        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> oplsConfigurationScheduleService.createParkingLotSystemSchedule(day3, endTime1, startTime1));
    }

    @Test
    public void testUpdateParkingLotSystemSchedule() {
        // test valid requests
        ParkingLotSystemSchedule plss1 = oplsConfigurationScheduleService.
                updateParkingLotSystemSchedule(day1, startTime2, endTime2);
        assertNotNull(plss1);
        assertEquals(day1, plss1.getDay());
        assertEquals(startTime2, plss1.getStartTime());
        assertEquals(endTime2, plss1.getEndTime());

        ParkingLotSystemSchedule plss2 = oplsConfigurationScheduleService.
                updateParkingLotSystemSchedule(day3, startTime2, endTime2);
        assertNotNull(plss1);
        assertEquals(day3, plss2.getDay());
        assertEquals(startTime2, plss2.getStartTime());
        assertEquals(endTime2, plss2.getEndTime());

        // test invalid request
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> oplsConfigurationScheduleService.updateParkingLotSystemSchedule(day1, endTime1, startTime1));
    }

    @Test
    public void testDeleteParkingLotSystemSchedule() {
        // test valid request
        oplsConfigurationScheduleService.deleteParkingLotSystemSchedule(day1);
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> oplsConfigurationScheduleService.getParkingLotSystemSchedule(day1));
        assertEquals(1, oplsConfigurationScheduleService.getAllParkingLotSystemSchedules().size());

        // test invalid request
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> oplsConfigurationScheduleService.deleteParkingLotSystemSchedule(day3));
    }
}
