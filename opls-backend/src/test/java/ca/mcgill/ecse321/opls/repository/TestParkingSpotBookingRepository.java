package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotBooking;

@SpringBootTest
public class TestParkingSpotBookingRepository {

	@Autowired
	private ParkingSpotRepository parkingSpotRepository;

	@Autowired
	private ParkingSpotBookingRepository parkingSpotBookingRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private UserAccountRepository userAccountRepository;

	@AfterEach
	public void clearDatabase() {
		parkingSpotBookingRepository.deleteAll();
		parkingSpotRepository.deleteAll();
		customerRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	private static ParkingSpotBooking newBooking(ParkingSpot ps,
			String licensePlate, Date start, int minuteDuration) {
		ParkingSpotBooking psb = ps.newBooking();
		psb.setLicensePlate("ABCD");
		psb.setDateRangeMinutes(start, 30);
		return psb;
	}

	/**
	 * Test creating and reading parking spot bookings.
	 */
	@Test
	public void testCreateRead() {
		// create parking spot and parking spot booking
		var cust = new Customer();
		cust.setUserAccount(userAccountRepository.save(TestUserAccountRepository.newAccount("john", "John", "Appleseed",
				"password", "Where are you happiest in the world?", "I don't know.")));
		cust = customerRepository.save(cust);
		var ps = parkingSpotRepository.save(new ParkingSpot('A', 15));
		var psb = newBooking(ps, "ABCD", new Date(), 30);
		psb.setCustomer(cust);
		psb = parkingSpotBookingRepository
				.save(psb);

		// validate results
		ParkingSpotBooking res = parkingSpotBookingRepository
				.findParkingSpotBookingByUuid(psb.getUuid());
		assertNotNull(res);
		assertEquals(psb.getId(), res.getId());
		assertEquals(psb.getLicensePlate(), res.getLicensePlate());

		assertTrue(parkingSpotBookingRepository.isParkingSpotBooked(ps));
		
		var results = parkingSpotBookingRepository.getCustomerActiveBookings(cust);
		int i = 0;
		for (var booking : results) {
			++i;
			assertEquals(psb.getId(), booking.getId());
			assertEquals(psb.getLicensePlate(), booking.getLicensePlate());
		}
		assertEquals(1, i);
	}

	/**
	 * Test updating parking spot bookings.
	 */
	@Test
	public void testUpdate() {
		var ps1 = parkingSpotRepository.save(new ParkingSpot('A', 15));
		var psId = ps1.getId();
		var psb = newBooking(ps1, "ABCD", new Date(), 30);
		String billingAccountId = "I AM POOR";
		psb.setBillingAccountId(billingAccountId);
		psb = parkingSpotBookingRepository.save(psb);

		// validate results
		ParkingSpotBooking res = parkingSpotBookingRepository
				.findParkingSpotBookingByUuid(psb.getUuid());
		assertNotNull(res);
		assertEquals(psId, res.getParkingSpot().getId());
		assertEquals(billingAccountId, res.getBillingAccountId());

		// update fields
		ps1 = parkingSpotRepository.save(new ParkingSpot('B', 30));
		String psId1 = ps1.getId();
		res.setParkingSpot(ps1);
		String newBillingId = "I AM NOT POOR ANYMORE";
		res.setBillingAccountId(newBillingId);
		res = parkingSpotBookingRepository.save(res);

		// validate results
		ParkingSpotBooking res2 = parkingSpotBookingRepository
				.findParkingSpotBookingByUuid(psb.getUuid());
		assertNotNull(res2);
		assertEquals(psId1, res2.getParkingSpot().getId());
		assertEquals(newBillingId, res2.getBillingAccountId());
	}

	/**
	 * Test deleting a parking spot booking.
	 */
	@Test
	public void testDelete() {
		var ps1 = parkingSpotRepository.save(new ParkingSpot('A', 15));
		var psId = ps1.getId();
		var psb = newBooking(ps1, "ABCD", new Date(), 30);
		String billingAccountId = "I AM POOR";
		psb.setBillingAccountId(billingAccountId);
		psb = parkingSpotBookingRepository.save(psb);
		UUID uuid = psb.getUuid();

		// validate results
		ParkingSpotBooking res = parkingSpotBookingRepository
				.findParkingSpotBookingByUuid(uuid);
		assertNotNull(res);
		assertEquals(psId, res.getParkingSpot().getId());
		assertEquals(billingAccountId, res.getBillingAccountId());

		// delete booking
		parkingSpotBookingRepository.delete(res);

		// try to get booking
		ParkingSpotBooking res1 = parkingSpotBookingRepository
				.findParkingSpotBookingByUuid(uuid);
		assertNull(res1);

		assertFalse(parkingSpotBookingRepository.isParkingSpotBooked(ps1));
	}

}
