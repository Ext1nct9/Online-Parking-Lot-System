package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.Booking;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.Booking.DateRange;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.model.VehicleService.VehicleServiceBooking;

@SpringBootTest
public class TestVehicleServiceBookingRepository {

	@Autowired
	private VehicleServiceRepository serviceRepo;

	@Autowired
	private VehicleServiceBookingRepository bookingRepo;

	@AfterEach
	public void clearDatabase() {
		bookingRepo.deleteAll();
		serviceRepo.deleteAll();
	}

	private static VehicleServiceBooking newBooking(VehicleService vs,
			String licensePlate, Date start) {
		var vsb = vs.newBooking(start);
		vsb.setLicensePlate(licensePlate);
		vsb.setBookingStatus(BookingStatus.CONFIRMED);
		return vsb;
	}

	/**
	 * Test querying vehicle service bookings by date range.
	 */
	@Test
	public void testFetchRange() {
		// create service
		var service = new VehicleService();
		service.setDisplayName("Tire change");
		service.setDuration(30);
		service = serviceRepo.save(service);

		Date start = new Date();
		Date now = start;

		// create bookings
		var ranges = new DateRange[2];
		var bookings = new VehicleServiceBooking[2];
		for (int i = 0; i < 2; i++) {
			bookings[i] = bookingRepo.save(newBooking(service, "ABCD", now));
			ranges[i] = bookings[i].getDateRange();
			now = ranges[i].endDate;
		}

		assertEquals(2,
				((ArrayList<VehicleServiceBooking>) bookingRepo.findAll())
						.size());

		// fetch over various ranges
		Iterable<DateRange> fetchedRanges;

		// fetch with range over first booking
		DateRange searchRange = DateRange.rangeWithStartAndDuration(start, 25,
				TimeUnit.MINUTES);
		fetchedRanges = bookingRepo.query(service, searchRange.startDate,
				searchRange.endDate);
		assertEquals(1, ((List<DateRange>) fetchedRanges).size());
		for (var booking : fetchedRanges) {
			assertEquals(bookings[0].getStartDate(), booking.startDate);
			assertEquals(bookings[0].getEndDate(), booking.endDate);
			assertEquals(bookings[0].getUuid(), booking.uuid);
		}

		// fetch with range over intersection
		searchRange = DateRange.rangeWithStartAndDuration(searchRange.endDate,
				20, TimeUnit.MINUTES);
		fetchedRanges = bookingRepo.query(service, searchRange.startDate,
				searchRange.endDate);
		assertEquals(2, ((List<DateRange>) fetchedRanges).size());

		// fetch with range over second booking
		searchRange = DateRange.rangeWithStartAndDuration(searchRange.endDate,
				50, TimeUnit.MINUTES);
		fetchedRanges = bookingRepo.query(service, searchRange.startDate,
				searchRange.endDate);
		assertEquals(1, ((List<DateRange>) fetchedRanges).size());
		for (var booking : fetchedRanges) {
			assertEquals(bookings[1].getStartDate(), booking.startDate);
			assertEquals(bookings[1].getEndDate(), booking.endDate);
			assertEquals(bookings[1].getUuid(), booking.uuid);
		}

		// fetch with range over both bookings
		searchRange = DateRange.rangeWithStartAndDuration(ranges[0].startDate,
				100, TimeUnit.MINUTES);
		fetchedRanges = bookingRepo.query(service, searchRange.startDate,
				searchRange.endDate);
		assertEquals(2, ((List<DateRange>) fetchedRanges).size());
	}

	/**
	 * Test fetching bookings for multiple services.
	 */
	@Test
	public void testFetchNamedRange() {
		// create services
		var services = new VehicleService[2];
		services[0] = new VehicleService();
		services[0].setDisplayName("Tire change");
		services[0].setDuration(30);
		services[0] = serviceRepo.save(services[0]);
		services[1] = new VehicleService();
		services[1].setDisplayName("Tire pressure");
		services[1].setDuration(50);
		services[1] = serviceRepo.save(services[1]);

		// create bookings
		var bookings = new VehicleServiceBooking[2];
		Date now = new Date();
		bookings[0] = bookingRepo.save(newBooking(services[0], "ABCDF", now));
		bookings[1] = bookingRepo.save(newBooking(services[1], "ABCDE", now));
		DateRange queryRange = DateRange.rangeWithStartAndDuration(now, 50,
				TimeUnit.MINUTES);

		// query
		var fetchedRanges = bookingRepo.query(services, queryRange.startDate,
				queryRange.endDate);
		assertEquals(2, ((List<DateRange>) fetchedRanges).size());
		for (var booking : fetchedRanges) {
			if (booking.getDurationMinutes() == services[0].getDuration()) {
				assertEquals(services[0].getDisplayName(), booking.name);
				assertEquals(bookings[0].getUuid(), booking.uuid);
			} else if (booking.getDurationMinutes() == services[1]
					.getDuration()) {
				assertEquals(services[1].getDisplayName(), booking.name);
				assertEquals(bookings[1].getUuid(), booking.uuid);
			} else {
				fail("Found unrecognized duration for service [" + booking.name
						+ "].");
			}
		}
		
		var ids = new ArrayList<String>();
		ids.add(services[0].getId());
		fetchedRanges = bookingRepo.query(ids, queryRange.startDate, queryRange.endDate);
		assertEquals(1, ((List<DateRange>) fetchedRanges).size());
		for (var booking : fetchedRanges) {
			assertEquals(bookings[0].getUuid(), booking.uuid);
		}
	}

	@Test
	public void testCreateRead() {
		var vehicleService = new VehicleService();
		vehicleService.setDisplayName("Car Wash");
		vehicleService.setFee(10);
		vehicleService.setId("wash");
		vehicleService.setDuration(30);
		vehicleService = serviceRepo.save(vehicleService);

		var vehicleServiceBooking = new VehicleService.VehicleServiceBooking();

		vehicleServiceBooking.setVehicleService(vehicleService);
		vehicleServiceBooking.setBookingStatus(Booking.BookingStatus.REQUESTED);
		vehicleServiceBooking.setStartDate(new Date());
		vehicleServiceBooking.setEndDate(new Date());
		vehicleServiceBooking.setLicensePlate("123j");
		vehicleServiceBooking.setConfirmationNumber("238325");

		vehicleServiceBooking = bookingRepo.save(vehicleServiceBooking);

		// Fetch and verify saved vehicle service booking

		Optional<VehicleService.VehicleServiceBooking> fetchedBooking = bookingRepo
				.findById(vehicleServiceBooking.getId());
		if (fetchedBooking.isPresent()) {
			assertEquals(fetchedBooking.get().getId(),
					vehicleServiceBooking.getId());
			assertEquals(fetchedBooking.get().getLicensePlate(),
					vehicleServiceBooking.getLicensePlate());
			assertEquals(fetchedBooking.get().getVehicleService().getId(),
					vehicleServiceBooking.getVehicleService().getId());
		} else {
			fail("VehicleServiceBooking not found");
		}
	}

}
