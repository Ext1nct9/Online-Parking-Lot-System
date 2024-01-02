package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Time;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import ca.mcgill.ecse321.opls.model.ParkingLotSystem.ParkingLotSystemSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;

@SpringBootTest
public class TestParkingLotSystemRepository {

	@Autowired
	private ParkingLotSystemRepository parkingLotSystemRepository;

	@Autowired
	private ParkingLotSystemScheduleRepository parkingLotSystemScheduleRepository;

	@AfterEach
	public void clearDatabase() {
		parkingLotSystemScheduleRepository.deleteAll();
		parkingLotSystemRepository.deleteAll();
	}

	public static ParkingLotSystem generateRandomConfig(String displayName) {
		Random r = new Random();

		ParkingLotSystem opls = new ParkingLotSystem();
		opls.setDisplayName(displayName);
		opls.setIncrementFee(r.nextDouble(1.0));
		opls.setIncrementTime(15);
		opls.setMaxIncrementTime(12 * 60);
		opls.setMonthlyFee(r.nextDouble(50.0, 65.0));

		return opls;
	}

	/**
	 * Test the creation of a parking lot system configuration
	 */
	@Test
	public void testCreation() {
		// create and set up parking lot system configuration
		double monthlyFee = 1234567.89;
		double incrementFee = 4.20;
		int incrementTime = 10;
		int maxIncrementTime = 100;
		String displayName = "lightning mcqueen";
		ParkingLotSystem pls = new ParkingLotSystem();
		pls.setMonthlyFee(monthlyFee);
		pls.setIncrementFee(incrementFee);
		pls.setIncrementTime(incrementTime);
		pls.setMaxIncrementTime(maxIncrementTime);
		pls.setDisplayName(displayName);
		pls.activate();
		String id = "plsTestCreation";
		pls.setId(id);

		pls = parkingLotSystemRepository.save(pls);

		// validate results
		var result = parkingLotSystemRepository.findParkingLotSystemById(id);
		assertNotNull(result);
		assertEquals(monthlyFee, result.getMonthlyFee());
		assertEquals(incrementFee, result.getIncrementFee());
		assertEquals(incrementTime, result.getIncrementTime());
		assertEquals(maxIncrementTime, result.getMaxIncrementTime());
		assertEquals(displayName, result.getDisplayName());
		assertTrue(result.isActive());
	}

	/**
	 * Test toggling activation status of a parking lot system configuration
	 */
	@Test
	public void testActivateDeactivate() {
		// create parking lot system and toggle on
		ParkingLotSystem pls = generateRandomConfig("cloudberry");
		pls.activate();

		pls = parkingLotSystemRepository.save(pls);
		String id = pls.getId();

		// validate results
		var result = parkingLotSystemRepository.findParkingLotSystemById(id);
		assertNotNull(result);
		assertEquals("cloudberry", result.getDisplayName());
		assertTrue(result.isActive());

		// toggle off
		result.deactivate();
		result = parkingLotSystemRepository.save(result);

		// validate results
		var result2 = parkingLotSystemRepository.findParkingLotSystemById(id);
		assertNotNull(result);
		assertEquals("cloudberry", result2.getDisplayName());
		assertFalse(result.isActive());
	}
	
	/**
	 * Test that only one parking lot system configuration can be active at
	 * once.
	 */
	@Test
	public void testUniqueActive() {
		var result = parkingLotSystemRepository.getActiveParkingLotSystem();
		assertNull(result);
		
		result = parkingLotSystemRepository.findParkingLotSystemById("Summer");
		assertNull(result);
		
		// Create active configuration
		ParkingLotSystem opls = generateRandomConfig("Summer");
		opls.activate();
		opls = parkingLotSystemRepository.save(opls);

		// get created configuration
		result = parkingLotSystemRepository.findParkingLotSystemById(opls.getId());
		assertNotNull(result);
		assertEquals(opls.getId(), result.getId());
		assertTrue(result.isActive());

		// get created configuration as active
		result = parkingLotSystemRepository.getActiveParkingLotSystem();
		assertNotNull(result);
		assertEquals(opls.getId(), result.getId());
		assertTrue(result.isActive());

		// create new configuration
		var opls2 = generateRandomConfig("Winter");
		opls2 = parkingLotSystemRepository.save(opls2);
		result = parkingLotSystemRepository.getActiveParkingLotSystem();
		assertEquals(opls.getId(), result.getId());

		// try to activate the new configuration
		opls2.activate();
		try {
			opls2 = parkingLotSystemRepository.save(opls2);
			fail("Able to activate multiple configurations.");
		} catch (Exception caught) {
		}
		
		result = parkingLotSystemRepository.getActiveParkingLotSystem();
		assertEquals(opls.getId(), result.getId());
		
		// try to activate the new configuration after deactivating the current configuration
		opls.deactivate();
		opls2.activate();
		opls = parkingLotSystemRepository.save(opls);
		opls2 = parkingLotSystemRepository.save(opls2);
		assertTrue(opls2.isActive());
		assertFalse(opls.isActive());
		result = parkingLotSystemRepository.getActiveParkingLotSystem();
		assertEquals(opls2.getId(), result.getId());
	}

	/**
	 * Test adding schedules to a parking lot system configuration
	 */
	@Test
	public void testParkingLotSystemWithSchedules() {
		// create parking lot system configuration and parking lot system schedules
		ParkingLotSystem pls = generateRandomConfig("krusty krab");
		pls = parkingLotSystemRepository.save(pls);
		String plsId = pls.getId();
		ParkingLotSystemSchedule plss1 = new ParkingLotSystemSchedule();
		ParkingLotSystemSchedule plss2 = new ParkingLotSystemSchedule();
		ParkingLotSystemSchedule plss3 = new ParkingLotSystemSchedule();
		HashSet<ParkingLotSystemSchedule> schedules = new HashSet<>();
		schedules.add(plss1);
		schedules.add(plss2);
		schedules.add(plss3);
		plss1.setDay(Day.MONDAY);
		plss2.setDay(Day.TUESDAY);
		plss3.setDay(Day.WEDNESDAY);
		HashSet<Day> days = new HashSet<>();
		days.add(Day.MONDAY);
		days.add(Day.TUESDAY);
		days.add(Day.WEDNESDAY);
		Time now = Time.valueOf("10:10:10");
		HashSet<Integer> scheduleIds = new HashSet<>();
		for (ParkingLotSystemSchedule plss: schedules) {
			plss.setParkingLotSystem(pls);
			plss.setStartTime(now);
			plss = parkingLotSystemScheduleRepository.save(plss);
			scheduleIds.add(plss.getId());
		}

		// validate parking lot system schedules
		var result = parkingLotSystemRepository.findParkingLotSystemById(plsId);
		assertNotNull(result);
		assertEquals("krusty krab", result.getDisplayName());
		Set<ParkingLotSystemSchedule> schedulesList = (Set<ParkingLotSystemSchedule>) result.getSchedules();
		assertEquals(3, schedulesList.size());
		for (ParkingLotSystemSchedule plss: schedulesList) {
			assertTrue(scheduleIds.contains(plss.getId()));
			scheduleIds.remove(plss.getId());
			assertTrue(days.contains(plss.getDay()));
			days.remove(plss.getDay());
			assertEquals(now, plss.getStartTime());
		}
		assertEquals(0, scheduleIds.size());
		assertEquals(0, days.size());
	}

}
