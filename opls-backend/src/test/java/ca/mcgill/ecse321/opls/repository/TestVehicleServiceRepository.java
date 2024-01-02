package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.VehicleService;

@SpringBootTest
public class TestVehicleServiceRepository {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ParkingSpotRepository parkingSpotRepository;

	@Autowired
	private ParkingSpotBookingRepository parkingSpotBookingRepository;

	@Autowired
	private VehicleServiceRepository vehicleServiceRepository;

	@Autowired
	private VehicleServiceBookingRepository vehicleServiceBookingRepository;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@AfterEach
	public void clearDatabase() {
		customerRepository.deleteAll();
		userAccountRepository.deleteAll();
		parkingSpotBookingRepository.deleteAll();
		parkingSpotRepository.deleteAll();
		vehicleServiceBookingRepository.deleteAll();
		vehicleServiceRepository.deleteAll();
	}
	// Method to create a new vehicle service
	private VehicleService newVehicleService(String displayName, int duration,
			Double fee) {
		var vehicleService = new VehicleService();
		vehicleService.setDisplayName(displayName);
		vehicleService.setDuration(duration);
		vehicleService.setFee(fee);
		return vehicleService;

	}

	@Test
	public void testCreation() {
		// create vehicle service
		String displayName = "Car Wash";
		int duration = 15;
		Double fee = 10.00;

		var vs = newVehicleService(displayName, duration, fee);
		vs = vehicleServiceRepository.save(vs);
		String id = vs.getId();

		// read service data using display name
		var result = vehicleServiceRepository.findVehicleServiceById(id);
		assertNotNull(result);
		assertEquals(displayName, result.getDisplayName());
		assertEquals(duration, result.getDuration());
		assertEquals(fee, result.getFee());

		// read data using id
		var resultID = vehicleServiceRepository.findVehicleServiceById(id);
		assertNotNull(resultID);
		assertEquals(displayName, resultID.getDisplayName());
		assertEquals(duration, resultID.getDuration());
		assertEquals(fee, resultID.getFee());
	}

	@Test
	public void testUpdate() {
		// create vehicle service
		String displayName = "Tire Change";
		int duration = 90;
		Double fee = 100.00;
		var vs = newVehicleService(displayName, duration, fee);
		assertEquals(duration, vs.getDuration());
		assertEquals(fee, vs.getFee());
		vs = vehicleServiceRepository.save(vs);

		// new values
		String newDisplayName = "Detailing";
		int newDuration = 300;
		Double newFee = 800.00;
		vs.setDisplayName(newDisplayName);
		vs.setDuration(newDuration);
		vs.setFee(newFee);
		vs = vehicleServiceRepository.save(vs);

		// test updated values
		String id2 = vs.getId();
		var updateResult = vehicleServiceRepository.findVehicleServiceById(id2);
		assertEquals(newDisplayName, updateResult.getDisplayName());
		assertEquals(newDuration, updateResult.getDuration());
		assertEquals(newFee, updateResult.getFee());
	}

	@Test
	public void testDelete() {
		// create vehicle service
		String displayName = "Wrap";
		int duration = 600;
		Double fee = 2000.00;
		var vs = newVehicleService(displayName, duration, fee);
		vs = vehicleServiceRepository.save(vs);
		String id = vs.getId();
		vehicleServiceRepository.delete(vs);
		// check if vehicle service was deleted
		assertNull(vehicleServiceRepository.findVehicleServiceById(id));
	}

}
