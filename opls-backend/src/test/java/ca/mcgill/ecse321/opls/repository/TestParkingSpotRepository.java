package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;

@SpringBootTest
public class TestParkingSpotRepository {

	@Autowired
	private ParkingSpotRepository parkingSpotRepository;

	@Autowired
	private ParkingSpotBookingRepository parkingSpotBookingRepository;

	@AfterEach
	public void clearDatabase() {
		parkingSpotBookingRepository.deleteAll();
		parkingSpotRepository.deleteAll();
	}

	private static void assertExists(Iterable<ParkingSpot> results, String id) {
		for (var ps0 : results) {
			if (ps0.getId().equals(id)) {
				return;
			}
		}
		fail("ID not found");
	}

	private static void assertExists(Iterable<ParkingSpot> results, String id,
			String message) {
		ParkingSpot result = null;
		for (var ps0 : results) {
			if (ps0.getId().equals(id)) {
				result = ps0;
				break;
			}
		}
		assertNotNull(result);
		assertEquals(message, result.getMessage());
	}

	private static void assertAbsent(Iterable<ParkingSpot> results, String id) {
		ParkingSpot result = null;
		for (var ps0 : results) {
			if (ps0.getId().equals(id)) {
				result = ps0;
				break;
			}
		}
		assertNull(result);
	}

	/**
	 * Test creating and querying a parking spot.
	 */
	@Test
	public void testCreateRead() {
		// create parking spot
		var message = "my message";
		var ps = new ParkingSpot('A', 15, VehicleType.REGULAR,
				ParkingSpotStatus.OPEN);
		ps.setMessage(message);
		ps = parkingSpotRepository.save(ps);

		// read spot data
		var result = parkingSpotRepository.findParkingSpotById("A015");
		assertNotNull(result);
		assertEquals(message, result.getMessage());
		result = parkingSpotRepository.findParkingSpotById('A', 15);
		assertEquals(message, result.getMessage());

		// ensure list queries find it
		assertExists(parkingSpotRepository.query(null, null, null), ps.getId(),
				message);
		assertExists(
				parkingSpotRepository.query(null, null,
						Arrays.asList(VehicleType.REGULAR)),
				ps.getId(), message);
		assertExists(parkingSpotRepository.query(new char[]{'A'}, null, null),
				ps.getId(), message);
		assertExists(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.OPEN), null),
				ps.getId(), message);
	}

	/**
	 * Test updating a parking spot.
	 */
	@Test
	public void testUpdate() {
		// create parking spot
		var message = "my message";
		var ps = new ParkingSpot('A', 15, VehicleType.REGULAR,
				ParkingSpotStatus.OPEN);
		ps.setMessage(message);
		ps = parkingSpotRepository.save(ps);

		// read spot data
		var result = parkingSpotRepository.findParkingSpotById("A015");
		assertNotNull(result);
		assertEquals(message, result.getMessage());
		assertEquals(VehicleType.REGULAR, result.getVehicleType());
		assertEquals(ParkingSpotStatus.OPEN, result.getParkingSpotStatus());

		// update parking spot
		String newMessage = "good morning";
		result.setMessage(newMessage);
		result.setVehicleType(VehicleType.LARGE);
		result.setParkingSpotStatus(ParkingSpotStatus.RESERVED);
		result = parkingSpotRepository.save(result);

		// read spot data again
		var result1 = parkingSpotRepository.findParkingSpotById("A015");
		assertNotNull(result1);
		assertEquals(newMessage, result1.getMessage());
		assertEquals(VehicleType.LARGE, result1.getVehicleType());
		assertEquals(ParkingSpotStatus.RESERVED,
				result1.getParkingSpotStatus());
	}

	/**
	 * Test deleting a parking spot.
	 */
	@Test
	public void testDelete() {
		// create parking spot
		var message = "my message";
		var ps = new ParkingSpot('A', 15, VehicleType.REGULAR,
				ParkingSpotStatus.OPEN);
		ps.setMessage(message);
		ps = parkingSpotRepository.save(ps);

		// read spot data
		var result = parkingSpotRepository.findParkingSpotById("A015");
		assertNotNull(result);
		assertEquals(message, result.getMessage());
		assertEquals(VehicleType.REGULAR, result.getVehicleType());
		assertEquals(ParkingSpotStatus.OPEN, result.getParkingSpotStatus());

		// delete parking spot
		parkingSpotRepository.delete(result);

		// try to find parking spot
		var result1 = parkingSpotRepository.findParkingSpotById("A015");
		assertNull(result1);
	}

	/**
	 * Test updating then querying a parking spot.
	 */
	@Test
	public void testUpdateWithQuery() {
		// create parking spot
		var message = "my message";
		var ps = new ParkingSpot('A', 15, VehicleType.REGULAR,
				ParkingSpotStatus.CLOSED);
		ps.setMessage(message);
		ps = parkingSpotRepository.save(ps);

		// ensure list queries for open spots do not find it
		assertAbsent(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.OPEN), null),
				ps.getId());
		assertAbsent(parkingSpotRepository.query(null,
				Arrays.asList(ParkingSpotStatus.OPEN),
				Arrays.asList(VehicleType.REGULAR)), ps.getId());
		assertAbsent(
				parkingSpotRepository.query(new char[]{'A'},
						Arrays.asList(ParkingSpotStatus.OPEN), null),
				ps.getId());

		// ensure specific queries do find it
		assertExists(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.CLOSED), null),
				ps.getId(), message);

		// set to reserved
		ps.setParkingSpotStatus(ParkingSpotStatus.RESERVED);
		parkingSpotRepository.save(ps);

		// ensure list queries for open spots do not find it
		assertAbsent(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.OPEN), null),
				ps.getId());
		assertAbsent(parkingSpotRepository.query(null,
				Arrays.asList(ParkingSpotStatus.OPEN),
				Arrays.asList(VehicleType.REGULAR)), ps.getId());
		assertAbsent(
				parkingSpotRepository.query(new char[]{'A'},
						Arrays.asList(ParkingSpotStatus.OPEN), null),
				ps.getId());

		// ensure specific queries do find it
		assertExists(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.RESERVED), null),
				ps.getId(), message);

		// set to open
		ps.setParkingSpotStatus(ParkingSpotStatus.OPEN);
		parkingSpotRepository.save(ps);

		// ensure list queries find it
		assertExists(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.OPEN), null),
				ps.getId());
		assertExists(parkingSpotRepository.query(null,
				Arrays.asList(ParkingSpotStatus.OPEN),
				Arrays.asList(VehicleType.REGULAR)), ps.getId());
		assertExists(parkingSpotRepository.query(new char[]{'A'}, null, null),
				ps.getId());

		// ensure specific queries do not find it
		assertAbsent(
				parkingSpotRepository.query(null,
						Arrays.asList(ParkingSpotStatus.RESERVED), null),
				ps.getId());
	}

	/**
	 * Test cases of the search query on parking spot.
	 */
	@Test
	public void testAdvancedQuery() {
		var ps_reg_open = parkingSpotRepository.save(new ParkingSpot('A', 15,
				VehicleType.REGULAR, ParkingSpotStatus.OPEN));
		var ps_reg_rese = parkingSpotRepository.save(new ParkingSpot('A', 16,
				VehicleType.REGULAR, ParkingSpotStatus.RESERVED));
		var ps_reg_clos = parkingSpotRepository.save(new ParkingSpot('A', 17,
				VehicleType.REGULAR, ParkingSpotStatus.CLOSED));
		var ps_lg_open = parkingSpotRepository.save(new ParkingSpot('A', 18,
				VehicleType.LARGE, ParkingSpotStatus.OPEN));
		var ps_lg_rese = parkingSpotRepository.save(new ParkingSpot('A', 19,
				VehicleType.LARGE, ParkingSpotStatus.RESERVED));
		var ps_lg_clos = parkingSpotRepository.save(new ParkingSpot('A', 20,
				VehicleType.LARGE, ParkingSpotStatus.CLOSED));

		Iterable<ParkingSpot> res;

		res = parkingSpotRepository.query(null, null, null);
		assertExists(res, ps_reg_open.getId());
		assertExists(res, ps_reg_rese.getId());
		assertExists(res, ps_reg_clos.getId());
		assertExists(res, ps_lg_open.getId());
		assertExists(res, ps_lg_rese.getId());
		assertExists(res, ps_lg_clos.getId());

		res = parkingSpotRepository.query(null,
				Arrays.asList(ParkingSpotStatus.OPEN), null);
		assertExists(res, ps_reg_open.getId());
		assertAbsent(res, ps_reg_rese.getId());
		assertAbsent(res, ps_reg_clos.getId());
		assertExists(res, ps_lg_open.getId());
		assertAbsent(res, ps_lg_rese.getId());
		assertAbsent(res, ps_lg_clos.getId());

		res = parkingSpotRepository.query(null,
				Arrays.asList(ParkingSpotStatus.OPEN),
				Arrays.asList(VehicleType.LARGE));
		assertAbsent(res, ps_reg_open.getId());
		assertAbsent(res, ps_reg_rese.getId());
		assertAbsent(res, ps_reg_clos.getId());
		assertExists(res, ps_lg_open.getId());
		assertAbsent(res, ps_lg_rese.getId());
		assertAbsent(res, ps_lg_clos.getId());

		res = parkingSpotRepository.query(new char[]{'A', 'B', 'C'},
				Arrays.asList(ParkingSpotStatus.CLOSED),
				Arrays.asList(VehicleType.LARGE));
		assertAbsent(res, ps_reg_open.getId());
		assertAbsent(res, ps_reg_rese.getId());
		assertAbsent(res, ps_reg_clos.getId());
		assertAbsent(res, ps_lg_open.getId());
		assertAbsent(res, ps_lg_rese.getId());
		assertExists(res, ps_lg_clos.getId());

		res = parkingSpotRepository.query(new char[]{'B'}, null, null);
		assertAbsent(res, ps_reg_open.getId());
		assertAbsent(res, ps_reg_rese.getId());
		assertAbsent(res, ps_reg_clos.getId());
		assertAbsent(res, ps_lg_open.getId());
		assertAbsent(res, ps_lg_rese.getId());
		assertAbsent(res, ps_lg_clos.getId());
	}

	/**
	 * Test querying parking spots with bookings involved.
	 */
	@Test
	public void testQueryBookings() {
		var ps_reg_open = parkingSpotRepository.save(new ParkingSpot('A', 15,
				VehicleType.REGULAR, ParkingSpotStatus.OPEN));
		var ps_reg_rese = parkingSpotRepository.save(new ParkingSpot('A', 16,
				VehicleType.REGULAR, ParkingSpotStatus.RESERVED));
		var ps_reg_clos = parkingSpotRepository.save(new ParkingSpot('A', 17,
				VehicleType.REGULAR, ParkingSpotStatus.CLOSED));
		var ps_lg_open = parkingSpotRepository.save(new ParkingSpot('A', 18,
				VehicleType.LARGE, ParkingSpotStatus.OPEN));
		var ps_lg_rese = parkingSpotRepository.save(new ParkingSpot('A', 19,
				VehicleType.LARGE, ParkingSpotStatus.RESERVED));
		var ps_lg_clos = parkingSpotRepository.save(new ParkingSpot('A', 20,
				VehicleType.LARGE, ParkingSpotStatus.CLOSED));

		Iterable<ParkingSpot> res;

		res = parkingSpotRepository.queryUnbooked(null, null, null);
		assertExists(res, ps_reg_open.getId());
		assertExists(res, ps_reg_rese.getId());
		assertExists(res, ps_reg_clos.getId());
		assertExists(res, ps_lg_open.getId());
		assertExists(res, ps_lg_rese.getId());
		assertExists(res, ps_lg_clos.getId());

		var psb = ps_reg_open.newBooking();
		psb.setLicensePlate("ABCD");
		psb.setStartDate(new Date());
		psb.setEndDate(
				new Date(new Date().getTime() + TimeUnit.MINUTES.toMillis(30)));
		psb = parkingSpotBookingRepository.save(psb);

		res = parkingSpotRepository.queryUnbooked(null, null, null);
		assertAbsent(res, ps_reg_open.getId());
		assertExists(res, ps_reg_rese.getId());
		assertExists(res, ps_reg_clos.getId());
		assertExists(res, ps_lg_open.getId());
		assertExists(res, ps_lg_rese.getId());
		assertExists(res, ps_lg_clos.getId());

		assertEquals(5,
				parkingSpotRepository.queryUnbookedCount(null, null, null));
		assertEquals(6,
				parkingSpotRepository.queryCount(null, null, null));
	}

}
