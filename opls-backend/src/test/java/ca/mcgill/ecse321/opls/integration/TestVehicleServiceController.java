package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.*;

import ca.mcgill.ecse321.opls.repository.VehicleServiceRepository;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.dto.service.VehicleServiceDto;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.service.VehicleServiceService;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestVehicleServiceController extends OplsApiTester {


	@Autowired
    private VehicleServiceService vehicleServiceService;


	private static final String id = "tire-repair", displayName = "tire repair", displayName2 = "oil change", id2 = "oil-change", notId = "notid", notDisplayName = "";

    private static final int duration = 15, duration2 = 5, notDuration = -1;

    private static final double fee = 50, fee2 = 20, notFee = -1;

    private VehicleService vehicleService;

	@Override
	@PostConstruct
	public void specifyAuthentication() {
		this.addDefaultClaim(OAuthClaim.ADMIN);
		this.addDefaultClaim(OAuthClaim.EMPLOYEE);
	}

	@Override
	@BeforeEach
	public void setupTestData() {
		vehicleService = vehicleServiceService.createVehicleService(displayName, duration, fee);
	}

	
	/**
     * Test endpoint GET /service/{id}
     */
    @Test
    public void testGetVehicleService() {
        // test valid request
        VehicleServiceDto response = this.exchange(HttpMethod.GET, MessageFormat.format(
                "/service/{0}", vehicleService.getId()), VehicleServiceDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(id, response.id);
        assertEquals(duration, response.duration);
        assertEquals(fee, response.fee);

        // test invalid request using invalid id
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found", 
        () -> vehicleServiceService.getVehicleService(notId));

        //test invalid request using empty id
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request", 
        () -> vehicleServiceService.getVehicleService(""));
    }

	/**
     * Test endpoint GET /service
     */
    @Test
    public void testGetAllVehicleServices() {

        VehicleServiceDto request = new VehicleServiceDto(displayName, fee, duration);
        VehicleServiceDto postResp = this.exchange(HttpMethod.POST, "/service", request, VehicleServiceDto.class,
                HttpStatus.OK).getBody();

        var classObj = new ArrayList<LinkedHashMap<?, ?>>().getClass();
        var response = this.exchange(HttpMethod.GET, "/service", classObj, HttpStatus.OK).getBody();
        var i = 0;
        ObjectMapper objectMapper = new ObjectMapper();
        

        assertNotNull(response);
		assertEquals(1, response.size());
        for (var obj : response) {
			++i;
			assertDtoEquals(postResp, objectMapper.convertValue(obj,
            VehicleServiceDto.class));
		}
        assertEquals(1, i);

    }

	/**
     * Test endpoint POST /service
     */
    @Test
    public void testCreateVehicleService() {
        // test valid request
		this.addDefaultClaim(OAuthClaim.ADMIN);
        VehicleServiceDto request = new VehicleServiceDto(displayName, fee, duration);

        VehicleServiceDto response = this.exchange(HttpMethod.POST, "/service", request, VehicleServiceDto.class,
                HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(displayName, response.displayName);
        assertEquals(fee, response.fee);
        assertEquals(duration, response.duration);
		
		// test invalid request using invalid display name
        request = new VehicleServiceDto(notDisplayName, fee, duration);

		var error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.POST, "/service", request,
                null);

        assertTrue(error.hasField("displayName", "The field must be at least 1 character!"));

		// test invalid request using invalid fee
		request = new VehicleServiceDto(displayName, notFee, duration);

		error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.POST, "/service", request,
                null);

        assertTrue(error.hasField("fee", "The fee must be a positive number greater than 0.0!"));

		// test invalid request using invalid duration
		request = new VehicleServiceDto(displayName, fee, notDuration);

		error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.POST, "/service", request,
                null);

        assertTrue(error.hasField("duration", "The duration must be a positive number greater than 0.0!"));
		
    }

	/**
     * Test endpoint PUT /service/{id}
     */
    @Test
    public void testUpdateVehicleService() {
        // test valid request
        VehicleServiceDto request = new VehicleServiceDto(displayName, fee2, duration2);

        VehicleServiceDto response = this.exchange(HttpMethod.PUT, MessageFormat.format("/service/{0}",
                vehicleService.getId()), request, VehicleServiceDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
		assertEquals(vehicleService.getId(), response.id);
		assertEquals(fee2, response.fee);
		assertEquals(duration2, response.duration);

		// test invalid request using invalid display name
        request = new VehicleServiceDto(notDisplayName, notFee, duration2);

        var error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.PUT, MessageFormat.format(
                        "/service/{0}", vehicleService.getId()), request, null);

        assertTrue(error.hasField("displayName", "The field must be at least 1 character!"));


		// test invalid request using invalid fee
        request = new VehicleServiceDto(displayName, notFee, duration2);

        error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.PUT, MessageFormat.format(
                        "/service/{0}", vehicleService.getId()), request, null);

        assertTrue(error.hasField("fee", "The fee must be a positive number greater than 0.0!"));

		// test invalid request using invalid duration
        request = new VehicleServiceDto(displayName, fee2, notDuration);

        error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
        "Invalid request body.", null, HttpMethod.PUT, MessageFormat.format(
                "/service/{0}", vehicleService.getId()), request, null);

        assertTrue(error.hasField("duration", "The duration must be a positive number greater than 0.0!"));
        
    }

	/**
     * Test endpoint DELETE /service/{id}
     */
    @Test
    public void testDeleteEmployee() {
        VehicleServiceDto response = this.exchange(HttpMethod.DELETE, MessageFormat.format("/service/{0}",
                vehicleService.getId()), VehicleServiceDto.class, HttpStatus.OK).getBody();

		assertEquals(vehicleService.getId(), response.id);
		assertEquals(vehicleService.getFee(), response.fee);
		assertEquals(vehicleService.getDuration(), response.duration);
        
		
    }

    public static void assertDtoEquals(VehicleServiceDto expected,
			VehicleServiceDto actual) {
		assertEquals(expected.id, actual.id);
		assertEquals(expected.displayName, actual.displayName);
		assertEquals(expected.fee, actual.fee);
		assertEquals(expected.duration, actual.duration);
		
	}

}
