package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotBooking;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.model.VehicleService.VehicleServiceBooking;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

@SpringBootTest
public class TestCustomerRepository {

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
		parkingSpotBookingRepository.deleteAll();
		parkingSpotRepository.deleteAll();
		vehicleServiceBookingRepository.deleteAll();
		vehicleServiceRepository.deleteAll();
		customerRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	private Customer newCustomerAndUser(String licensePlate,
			String billingAccountId, String username, String firstName,
			String lastName) {
		var userAccount = userAccountRepository.save(TestUserAccountRepository
				.newAccount(username, firstName, lastName, "password",
						"Where are you happiest in the world?", "My bed"));

		var customer = new Customer();
		customer.setSavedLicensePlate(licensePlate);
		customer.setSavedBillingAccountId(billingAccountId);
		customer.setUserAccount(userAccount);
		customer = customerRepository.save(customer);

		return customer;
	}

	/**
	 * Test the creation of a customer with an associated user account
	 */
	@Test
	public void testCreationWithUserAccount() {
		// create user account and customer entries
		String licensePlate = "I AM SPEED";
		String billingAccountId = "I AM POOR";
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var customer = newCustomerAndUser(licensePlate, billingAccountId,
				username, firstName, lastName);
		int id = customer.getId();

		// fetch customer
		var result = customerRepository.findCustomerById(id);
		assertNotNull(result);
		assertEquals(licensePlate, result.getSavedLicensePlate());
		assertEquals(billingAccountId, result.getSavedBillingAccountId());

		// validate linked user
		UserAccount resultUserAccount = result.getUserAccount();
		assertNotNull(resultUserAccount);
		assertEquals(username, resultUserAccount.getUsername());
		assertEquals(firstName, resultUserAccount.getFirstName());
		assertEquals(lastName, resultUserAccount.getLastName());
		
		// validate customer from user account
		resultUserAccount = userAccountRepository.findUserAccountByUsername(username);
		assertNotNull(resultUserAccount);
		result = customerRepository.findCustomerByUserAccount(resultUserAccount);
		assertNotNull(result);
		assertEquals(id, result.getId());
		assertEquals(billingAccountId, result.getSavedBillingAccountId());
		
		result = resultUserAccount.getCustomer();
		assertNotNull(result);
		assertEquals(id, result.getId());
		assertEquals(billingAccountId, result.getSavedBillingAccountId());
	}

	/**
	 * Test updating a customer with new details and a new user account
	 */
	@Test
	public void testUpdate() {
		// create user account and customer
		String licensePlate = "I AM SPEED";
		String billingAccountId = "I AM POOR";
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var customer = newCustomerAndUser(licensePlate, billingAccountId,
				username, firstName, lastName);
		int id = customer.getId();

		// fetch customer
		var result = customerRepository.findCustomerById(id);
		assertNotNull(result);
		assertEquals(licensePlate, result.getSavedLicensePlate());
		assertEquals(billingAccountId, result.getSavedBillingAccountId());

		// validate linked user
		UserAccount resultUserAccount = result.getUserAccount();
		assertNotNull(resultUserAccount);
		assertEquals(username, resultUserAccount.getUsername());
		assertEquals(firstName, resultUserAccount.getFirstName());
		assertEquals(lastName, resultUserAccount.getLastName());

		// change details of customer
		String newLicensePlate = "new license plate";
		String newBillingAccountId = "I AM NOT POOR ANYMORE";
		String newUsername = "squidward";
		String newFirstName = "krusty";
		String newLastName = "krab";
		var userAccount = userAccountRepository.save(TestUserAccountRepository
				.newAccount(newUsername, newFirstName, newLastName, "password",
						"Where are you happiest in the world?", "My bed"));
		int newAccountId = userAccount.getId();
		result.setSavedLicensePlate(newLicensePlate);
		result.setSavedBillingAccountId(newBillingAccountId);
		result.setUserAccount(userAccount);
		result = customerRepository.save(result);

		// fetch customer again
		var result2 = customerRepository.findCustomerById(id);
		assertNotNull(result);
		assertEquals(newLicensePlate, result2.getSavedLicensePlate());
		assertEquals(newBillingAccountId, result2.getSavedBillingAccountId());

		// validate linked user
		assertEquals(newAccountId, result2.getUserAccount().getId());
	}

	/**
	 * Test deleting a customer from the database
	 */
	@Test
	public void testDelete() {
		// create user account and customer entries
		String licensePlate = "I AM SPEED";
		String billingAccountId = "I AM POOR";
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var customer = newCustomerAndUser(licensePlate, billingAccountId,
				username, firstName, lastName);
		int id = customer.getId();

		// delete customer
		customerRepository.delete(customer);

		// try to get customer
		var result = customerRepository.findCustomerById(id);
		assertNull(result);
	}

	/**
	 * Test adding parking spot bookings to a customer
	 */
	@Test
	public void testCustomerWithParkingSpotBookings() {
		// create user account and customer entries
		String licensePlate = "I AM SPEED";
		String billingAccountId = "I AM POOR";
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var customer = newCustomerAndUser(licensePlate, billingAccountId,
				username, firstName, lastName);
		int customerId = customer.getId();

		// create parking spots
		ParkingSpot ps1 = parkingSpotRepository.save(new ParkingSpot('1', 101,
				VehicleType.REGULAR, ParkingSpotStatus.OPEN));
		ParkingSpot ps2 = parkingSpotRepository.save(new ParkingSpot('2', 222,
				VehicleType.LARGE, ParkingSpotStatus.OPEN));
		ParkingSpot ps3 = parkingSpotRepository.save(new ParkingSpot('3', 420,
				VehicleType.REGULAR, ParkingSpotStatus.OPEN));

		// create bookings for the parking spots
		ParkingSpotBooking psb1 = ps1.newBooking();
		ParkingSpotBooking psb2 = ps2.newBooking();
		ParkingSpotBooking psb3 = ps3.newBooking();

		// set properties for each booking
		var bookingReferences = new ArrayList<ParkingSpotBooking>();
		bookingReferences.add(psb1);
		bookingReferences.add(psb2);
		bookingReferences.add(psb3);
		Date now = new Date();
		for (ParkingSpotBooking psb : bookingReferences) {
			psb.setStartDate(now);
			psb.setEndDate(now);
			psb.setCustomer(customer);
			psb.setLicensePlate(licensePlate);
		}
		psb1.setBillingAccountId("I AM POOR 1");
		psb2.setBillingAccountId("I AM POOR 2");
		psb3.setBillingAccountId("I AM POOR 3");

		// save to database
		psb1 = parkingSpotBookingRepository.save(psb1);
		psb2 = parkingSpotBookingRepository.save(psb2);
		psb3 = parkingSpotBookingRepository.save(psb3);

		// save information to compare against results
		HashSet<String> billingAccountIds = new HashSet<>();
		billingAccountIds.add("I AM POOR 1");
		billingAccountIds.add("I AM POOR 2");
		billingAccountIds.add("I AM POOR 3");

		HashSet<String> parkingSpotIds = new HashSet<>();
		parkingSpotIds.add(ps1.getId());
		parkingSpotIds.add(ps2.getId());
		parkingSpotIds.add(ps3.getId());

		var result = customerRepository.findCustomerById(customerId);
		assertNotNull(result);

		// validate bookings in list
		var bookingsList = parkingSpotBookingRepository.findParkingSpotBookingsByCustomer(result);
		int i = 0;
		for (ParkingSpotBooking psb : bookingsList) {
			assertTrue(billingAccountIds.contains(psb.getBillingAccountId()));
			billingAccountIds.remove(psb.getBillingAccountId());
			assertTrue(parkingSpotIds.contains(psb.getParkingSpot().getId()));
			parkingSpotIds.remove(psb.getParkingSpot().getId());
			++i;
		}
		assertEquals(3, i);
		assertEquals(0, billingAccountIds.size());
		assertEquals(0, parkingSpotIds.size());
	}

	/**
	 * Test adding vehicle service bookings to a customer
	 */
	@Test
	public void testCustomerWithVehicleServiceBookings() {
		// create user account and customer entries
		String licensePlate = "I AM SPEED";
		String billingAccountId = "I AM POOR";
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var customer = newCustomerAndUser(licensePlate, billingAccountId,
				username, firstName, lastName);
		int customerId = customer.getId();

		// create vehicle services
		VehicleService vs1 = new VehicleService();
		VehicleService vs2 = new VehicleService();
		VehicleService vs3 = new VehicleService();
		vs1.setDuration(10);
		vs2.setDuration(20);
		vs3.setDuration(30);
		vs1.setFee(100);
		vs2.setFee(200);
		vs3.setFee(300);
		vs1.setDisplayName("krusty krab");
		vs2.setDisplayName("spring bean");
		vs3.setDisplayName("cloudberry");
		HashSet<String> displayNames = new HashSet<>();
		displayNames.add("krusty krab");
		displayNames.add("spring bean");
		displayNames.add("cloudberry");
		vs1 = vehicleServiceRepository.save(vs1);
		vs2 = vehicleServiceRepository.save(vs2);
		vs3 = vehicleServiceRepository.save(vs3);

		// create bookings
		Date now = new Date();
		VehicleServiceBooking vsb1 = vs1.newBooking(now);
		VehicleServiceBooking vsb2 = vs2.newBooking(now);
		VehicleServiceBooking vsb3 = vs3.newBooking(now);
		ArrayList<VehicleServiceBooking> bookingReferences = new ArrayList<>();
		bookingReferences.add(vsb1);
		bookingReferences.add(vsb2);
		bookingReferences.add(vsb3);
		vsb1.setVehicleService(vs1);
		vsb2.setVehicleService(vs2);
		vsb3.setVehicleService(vs3);
		for (VehicleServiceBooking vsb : bookingReferences) {
			vsb.setCustomer(customer);
			vsb.setLicensePlate(licensePlate);
		}

		HashSet<String> vehicleServiceIds = new HashSet<>();
		vehicleServiceIds.add(vs1.getId());
		vehicleServiceIds.add(vs2.getId());
		vehicleServiceIds.add(vs3.getId());
		vsb1 = vehicleServiceBookingRepository.save(vsb1);
		vsb2 = vehicleServiceBookingRepository.save(vsb2);
		vsb3 = vehicleServiceBookingRepository.save(vsb3);

		var result = customerRepository.findCustomerById(customerId);
		assertNotNull(result);

		// validate bookings in list
		var bookingsList = vehicleServiceBookingRepository.findVehicleServiceBookingsByCustomer(result);
		int i = 0;
		for (VehicleServiceBooking vsb : bookingsList) {
			assertTrue(displayNames
					.contains(vsb.getVehicleService().getDisplayName()));
			displayNames.remove(vsb.getVehicleService().getDisplayName());
			assertTrue(vehicleServiceIds
					.contains(vsb.getVehicleService().getId()));
			vehicleServiceIds.remove(vsb.getVehicleService().getId());
			++i;
		}
		assertEquals(3, i);
		assertEquals(0, displayNames.size());
		assertEquals(0, vehicleServiceIds.size());
	}
	
}

/**
 * 	 There are zero impostors among us
 *
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣤⣤⣤⣤⣤⣶⣦⣤⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣿⡿⠛⠉⠙⠛⠛⠛⠛⠻⢿⣿⣷⣤⡀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⣼⣿⠋⠀⠀⠀⠀⠀⠀⠀⢀⣀⣀⠈⢻⣿⣿⡄⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⣸⣿⡏⠀⠀⠀⣠⣶⣾⣿⣿⣿⠿⠿⠿⢿⣿⣿⣿⣄⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⣿⣿⠁⠀⠀⢰⣿⣿⣯⠁⠀⠀⠀⠀⠀⠀⠀⠈⠙⢿⣷⡄⠀
 * ⠀⠀⣀⣤⣴⣶⣶⣿⡟⠀⠀⠀⢸⣿⣿⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣷⠀
 * ⠀⢰⣿⡟⠋⠉⣹⣿⡇⠀⠀⠀⠘⣿⣿⣿⣿⣷⣦⣤⣤⣤⣶⣶⣶⣶⣿⣿⣿⠀
 * ⠀⢸⣿⡇⠀⠀⣿⣿⡇⠀⠀⠀⠀⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠃⠀
 * ⠀⣸⣿⡇⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠉⠻⠿⣿⣿⣿⣿⡿⠿⠿⠛⢻⣿⡇⠀⠀
 * ⠀⣿⣿⠁⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣧⠀⠀
 * ⠀⣿⣿⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⠀⠀
 * ⠀⣿⣿⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⠀⠀
 * ⠀⢿⣿⡆⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⡇⠀⠀
 * ⠀⠸⣿⣧⡀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠃⠀⠀
 * ⠀⠀⠛⢿⣿⣿⣿⣿⣇⠀⠀⠀⠀⠀⣰⣿⣿⣷⣶⣶⣶⠶⠀ ⣿⣿⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⣿⣿⡇⠀⣽⣿⡏⠁⠀⠀⢸⣿⡇⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⣿⣿⡇⠀⢹⣿⡆⠀⠀⠀⣸⣿⠇⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⢿⣿⣦⣄⣀⣠⣴⣿⣿⠁⠀⠈⠻⣿⣿⣿⣿⡿⠏⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠈⠛⠻⠿⠿⠿⠿⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
 */