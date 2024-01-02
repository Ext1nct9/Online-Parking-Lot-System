package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotDto;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotQueryRequestDto;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotQueryResponseDto;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestParkingSpotController extends OplsApiTester{

    @Autowired
    private OplsStartupService startupService;

    @Override
    @PostConstruct
    public void specifyAuthentication() {
        this.addDefaultClaim(OAuthClaim.ADMIN);
    }

    @Override
    public void setupTestData() {
    }

    @AfterEach
    public void clearDatabase() throws Exception {
        startupService.startupTest();
    }

    @BeforeEach
    public void setupData() {
        startupService.initializeConfiguration();
        startupService.initializeParkingLotStructure();
    }

    /**
     * Test endpoint POST /spot
     */
    @Test
    public void testCreateSpotRequest(){
        // create the request body
        var spot = new ParkingSpot('A',25);
        spot.setMessage("hello");
        spot.setParkingSpotStatus(ParkingSpot.ParkingSpotStatus.OPEN);
        spot.setVehicleType(ParkingSpot.VehicleType.LARGE);
        var requestBody = new ParkingSpotDto(spot);
        // response
        var response = this.exchange(HttpMethod.POST,"/spot",requestBody,ParkingSpotDto.class, HttpStatus.OK).getBody();
        assertEquals(spot.getId(), response.id);
        assertEquals(spot.getVehicleType(), response.vehicleType);
        assertEquals(spot.getMessage(), response.message);
        assertEquals(spot.getParkingSpotStatus(),response.parkingSpotStatus);

        // invalid request (id doesnt respect the size)
        requestBody.id = "0293021930";
        assertReturnsError(HttpStatus.BAD_REQUEST, "Invalid request body.", null, HttpMethod.POST, "/spot",requestBody,null);
    }
    /**
     * Test endpoint GET /spot/{id}
     */
    @Test
    public void testGetSpotRequest(){
        // create a spot
        var spot = new ParkingSpot('A',25);
        spot.setMessage("hello");
        spot.setParkingSpotStatus(ParkingSpot.ParkingSpotStatus.OPEN);
        spot.setVehicleType(ParkingSpot.VehicleType.LARGE);
        var requestBody = new ParkingSpotDto(spot);
        this.exchange(HttpMethod.POST,"/spot",requestBody,ParkingSpotDto.class, HttpStatus.OK);

        // get the spot
        var response = this.exchange(HttpMethod.GET,"/spot/"+spot.getId(),ParkingSpotDto.class,HttpStatus.OK).getBody();
        assertEquals(spot.getId(), response.id);
        assertEquals(spot.getVehicleType(), response.vehicleType);
        assertEquals(spot.getMessage(), response.message);
        assertEquals(spot.getParkingSpotStatus(),response.parkingSpotStatus);

        //testing exception catching with non-existing id
        assertReturnsError(HttpStatus.NOT_FOUND, "Parking spot not found.", null, HttpMethod.GET, "/spot/0000",null);

    }
    /**
     * Test endpoint PUT /spot/{id}
     */
    @Test
    public void testUpdateSpotRequest(){
        // create a spot
        var spot = new ParkingSpot('A',25);
        spot.setMessage("hello");
        spot.setParkingSpotStatus(ParkingSpot.ParkingSpotStatus.OPEN);
        spot.setVehicleType(ParkingSpot.VehicleType.LARGE);
        var requestBody = new ParkingSpotDto(spot);
        this.exchange(HttpMethod.POST,"/spot",requestBody,ParkingSpotDto.class, HttpStatus.OK);

        // update the spot
        requestBody.message = "hi"; // changed message
        requestBody.id = "0001"; //changed id
        var response = this.exchange(HttpMethod.PUT,"/spot/"+spot.getId(),requestBody,ParkingSpotDto.class,HttpStatus.OK).getBody();
        assertEquals(response.id, requestBody.id);
        assertEquals(spot.getVehicleType(), response.vehicleType);
        assertEquals(requestBody.message, response.message);
        assertEquals(spot.getParkingSpotStatus(),response.parkingSpotStatus);

        //testing exception catching with non-existing id since it was updated
        assertReturnsError(HttpStatus.NOT_FOUND, "Parking spot not found.", null, HttpMethod.PUT, "/spot/"+spot.getId(),requestBody,null);

    }
    /**
     * Test endpoint DELETE /spot/{id}
     */
    @Test
    public void testDeleteSpotRequest(){
        // create a spot
        var spot = new ParkingSpot('A',25);
        spot.setMessage("hello");
        spot.setParkingSpotStatus(ParkingSpot.ParkingSpotStatus.OPEN);
        spot.setVehicleType(ParkingSpot.VehicleType.LARGE);
        var requestBody = new ParkingSpotDto(spot);
        this.exchange(HttpMethod.POST,"/spot",requestBody,ParkingSpotDto.class, HttpStatus.OK);

        // delete the spot
        var response = this.exchange(HttpMethod.DELETE,"/spot/"+spot.getId(),ParkingSpotDto.class,HttpStatus.OK).getBody();
        assertEquals(spot.getId(), response.id);
        assertEquals(spot.getVehicleType(), response.vehicleType);
        assertEquals(spot.getMessage(), response.message);
        assertEquals(spot.getParkingSpotStatus(),response.parkingSpotStatus);

        //testing exception catching with non-existing id since we just deleted it
        assertReturnsError(HttpStatus.NOT_FOUND, "Parking spot not found.", null, HttpMethod.DELETE, "/spot/"+spot.getId(),null);

    }

    /**
     * Test endpoint POST /spot/search
     */
    @Test
    public void testSearchSpotRequest() throws Exception {
		// create a spot
		var spot2 = new ParkingSpot('L', 26);
		spot2.setMessage("hello2");
		spot2.setParkingSpotStatus(ParkingSpot.ParkingSpotStatus.OPEN);
		spot2.setVehicleType(ParkingSpot.VehicleType.REGULAR);
		var requestBody = new ParkingSpotDto(spot2);
		this.exchange(HttpMethod.POST, "/spot", requestBody,
				ParkingSpotDto.class, HttpStatus.OK);

		// search for an open spot on floor L
		var request = new ParkingSpotQueryRequestDto();
		request.floors = new ArrayList<Character>();
		request.floors.add('L');
		request.unbooked = true;
		var response = this
				.exchange(HttpMethod.POST, "/spot/search", request,
						ParkingSpotQueryResponseDto.class, HttpStatus.OK)
				.getBody();
		assertEquals(1, response.count);
		var foundSpotDto = ((ArrayList<ParkingSpotDto>) (response.parkingSpots))
				.get(0);
		assertEquals(foundSpotDto.id, requestBody.id); // check for spot2
        
        // query count
		response = this
				.exchange(HttpMethod.POST, "/spot/search/count", request,
						ParkingSpotQueryResponseDto.class, HttpStatus.OK)
				.getBody();
		assertEquals(1, response.count);
		assertNull(response.parkingSpots);
    }

}
