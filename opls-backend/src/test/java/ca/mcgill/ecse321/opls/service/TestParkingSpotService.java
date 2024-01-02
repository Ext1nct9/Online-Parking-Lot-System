package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotQueryRequestDto;
import ca.mcgill.ecse321.opls.model.Booking.BookingStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import ca.mcgill.ecse321.opls.repository.ParkingSpotRepository;

/**
 * Test the ParkingSpotService class.
 */
@ExtendWith(MockitoExtension.class)
public class TestParkingSpotService {

	@Mock
	private ParkingSpotRepository spotRepository;

	@InjectMocks
	private ParkingSpotService service;

	private static final String MY_PS_ID = "A035";
	private static final String NOT_FOUND_PS_ID = "C035";
	private static final VehicleType MY_VEHICLE_TYPE = VehicleType.REGULAR;

	@BeforeEach
	public void setupMocks() {
		var ps = new ParkingSpot('A', 35, MY_VEHICLE_TYPE,
				ParkingSpotStatus.OPEN);

		lenient().when(spotRepository.findParkingSpotById(MY_PS_ID))
				.thenAnswer((InvocationOnMock invocation) -> ps);

		lenient().when(spotRepository.findParkingSpotById(NOT_FOUND_PS_ID))
				.thenAnswer((InvocationOnMock invocation) -> null);
	}
	
	/**
	 * Test creating a parking spot.
	 */
	@Test
	public void testCreateParkingSpot() {
		var ps = new ParkingSpot('A', 35);
		ps.setMessage("Hello, world!");

		lenient().when(spotRepository.save(any(ParkingSpot.class)))
				.thenAnswer((InvocationOnMock invocation) -> invocation
						.getArgument(0));

		// valid request
		var resp = service.createParkingSpot(ps);
		assertNotNull(resp);
		assertEquals(ps.getId(), resp.getId());
		assertEquals(ps.getMessage(), resp.getMessage());
		
		verify(spotRepository, times(1)).save(resp);
	}

	/**
	 * Test fetching a parking spot.
	 */
	@Test
	public void testGetParkingSpot() {

		// test valid request
		var response = service.getParkingSpot(MY_PS_ID);
		assertNotNull(response);
		assertEquals(MY_VEHICLE_TYPE, response.getVehicleType());

		// test not found request
		assertThrowsApiException(HttpStatus.NOT_FOUND,
				"Parking spot not found.",
				() -> service.getParkingSpot(NOT_FOUND_PS_ID));
	}
	
	/**
	 * Test querying for parking spots.
	 */
	@Test
	public void testQuery() {
		var ps1 = new ParkingSpot('A', 35);
		var ps2 = new ParkingSpot('B', 35);
		var ps3 = new ParkingSpot('C', 35);
		
		var unbooked = Arrays.asList(ps1, ps2);
		var all = Arrays.asList(ps1, ps2, ps3);
		
		lenient()
			.when(spotRepository.queryUnbooked(null, null, null))
			.thenAnswer((InvocationOnMock invocation) -> unbooked);
		lenient()
			.when(spotRepository.query(null, null, null))
			.thenAnswer((InvocationOnMock invocation) -> all);
		
		// execute query for unbooked spots
		var req = new ParkingSpotQueryRequestDto();
		req.unbooked = true;
		var response = service.query(req);
		assertNotNull(response);
		int i = 0;
		for (var ps : response) {
			++i;
			assertTrue(unbooked.contains(ps));
		}
		assertEquals(unbooked.size(), i);
		
		// execute query for all spots
		req.unbooked = false;
		response = service.query(req);
		assertNotNull(response);
		i = 0;
		for (var ps : response) {
			++i;
			assertTrue(all.contains(ps));
		}
		assertEquals(all.size(), i);
	}
	/**
	 * Test updating a parking spot.
	 */
	@Test
	public void testUpdateParkingSpot(){
		//executing the method
		var spot = new ParkingSpot('A', 35);
		spot.setMessage("hello");
		spot.setParkingSpotStatus(ParkingSpotStatus.OPEN);
		spot.setVehicleType(VehicleType.LARGE);
		var res = service.updateParkingSpot(MY_PS_ID,new ParkingSpotDto(spot));
		// verifying the method
		assertEquals(spot.getId(),res.getId());
		assertEquals(spot.getParkingSpotStatus(),res.getParkingSpotStatus());
		assertEquals(spot.getMessage(),res.getMessage());
		assertEquals(spot.getVehicleType(),res.getVehicleType());
	}
	/**
	 * Test deleting a parking spot.
	 */
	@Test
	public void testDeleteParkingSpot(){
		var spot = spotRepository.findParkingSpotById(MY_PS_ID);
		//executing the method
		var res = service.deleteParkingSpot(MY_PS_ID);
		// verifying the method
		assertEquals(spot.getId(),res.getId());
		assertEquals(spot.getParkingSpotStatus(),res.getParkingSpotStatus());
		assertEquals(spot.getMessage(),res.getMessage());
		assertEquals(spot.getVehicleType(),res.getVehicleType());
	}
}
