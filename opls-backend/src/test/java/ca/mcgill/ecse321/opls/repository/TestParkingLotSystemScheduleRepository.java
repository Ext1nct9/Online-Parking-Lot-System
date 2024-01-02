package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem.ParkingLotSystemSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;

@SpringBootTest
public class TestParkingLotSystemScheduleRepository {

	@Autowired
	private ParkingLotSystemScheduleRepository scheduleRepository;
	
	@Autowired
	private ParkingLotSystemRepository systemRepository;
	
	@AfterEach
	public void clearDatabase() {
		scheduleRepository.deleteAll();
		systemRepository.deleteAll();
	}
	
	/**
	 * Test creating and querying parking lot schedules
	 */
	@Test
	public void testCreateAndQuery() {
		var result = systemRepository.getActiveParkingLotSystem();
		assertNull(result);
		
		// create parking lot system
		var system = TestParkingLotSystemRepository.generateRandomConfig("krusty krab");
		system.activate();
		system = systemRepository.save(system);
		
		var map = new HashMap<Day, ParkingLotSystemSchedule>();
		
		// create schedules
		int startHr = 10;
		for (var day : Day.values()) {
			map.put(day, scheduleRepository.save(system.addSchedule(day, startHr + ":00:00", (startHr + 5) + ":00:00")));
			++startHr;
		}
		
		// fetch schedules through system object
		result = systemRepository.findParkingLotSystemById(system.getId());
		assertNotNull(result);
		assertNotNull(result.getSchedules());
		for (var sched : result.getSchedules()) {
			Day day = sched.getDay();
			var storedSched = map.get(day);
			assertEquals(storedSched.getId(), sched.getId());
			assertEquals(storedSched.getStartTime(), sched.getStartTime());
			assertEquals(storedSched.getEndTime(), sched.getEndTime());
		}
		
		// fetch schedules with custom query
		var schedules = scheduleRepository.findActiveParkingLotSchedules();
		assertNotNull(schedules);
		for (var sched : schedules) {
			Day day = sched.getDay();
			var storedSched = map.get(day);
			assertEquals(storedSched.getId(), sched.getId());
			assertEquals(storedSched.getStartTime(), sched.getStartTime());
			assertEquals(storedSched.getEndTime(), sched.getEndTime());
		}
		
		// fetch schedules by day
		for (var day : Day.values()) {
			var sched = scheduleRepository.findActiveParkingLotScheduleByDay(day);
			var storedSched = map.get(day);
			assertNotNull(sched);
			assertEquals(storedSched.getId(), sched.getId());
			assertEquals(storedSched.getStartTime(), sched.getStartTime());
			assertEquals(storedSched.getEndTime(), sched.getEndTime());
		}
	}
	
}
