package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse321.opls.dto.OplsConfigurationDto;
import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import ca.mcgill.ecse321.opls.service.OplsConfigurationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestOplsConfigurationController extends OplsApiTester {

    @Autowired
    private OplsStartupService startupService;

    private static final Double monthlyFee = 12.34, incrementFee = 56.78;

    private static final int incrementTime = 150, maxIncrementTime = 250;

    @Override
    @PostConstruct
    public void specifyAuthentication() {
        this.addDefaultClaim(OAuthClaim.ADMIN);
    }

    @Override
    @BeforeEach
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
     * Test endpoint GET /config
     */
    @Test
    public void testGetActiveConfiguration() {
        // test valid request
        OplsConfigurationDto response = this.exchange(HttpMethod.GET, "/config",
                OplsConfigurationDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(startupService.getMonthlyFee(), response.getMonthlyFee());
        assertEquals(startupService.getIncrementFee(), response.getIncrementFee());
        assertEquals(startupService.getIncrementTime(), response.incrementTime);
        assertEquals(startupService.getMaxIncrementTime(), response.maxIncrementTime);

        // there are no invalid request cases
    }

    /**
     * Test endpoint PUT /config
     */
    @Test
    public void testUpdateActiveConfiguration() {
        // test valid request
        OplsConfigurationDto request = new OplsConfigurationDto(monthlyFee, incrementFee, incrementTime, maxIncrementTime);

        OplsConfigurationDto response = this.exchange(HttpMethod.PUT, "/config",
                request, OplsConfigurationDto.class, HttpStatus.OK).getBody();

        assertNotNull(response);
        assertEquals(monthlyFee, response.getMonthlyFee());
        assertEquals(incrementFee, response.getIncrementFee());
        assertEquals(incrementTime, response.incrementTime);
        assertEquals(maxIncrementTime, response.maxIncrementTime);

        // test invalid request
        request = new OplsConfigurationDto(-1.0, -1.0, -1, -1);

        var error = this.assertReturnsError(HttpStatus.BAD_REQUEST,
                "Invalid request body.", null, HttpMethod.PUT, "/config", request,
                null);

        assertTrue(error.hasField("monthlyFee",
                "The monthly fee must be a positive number greater than 0.0!"));
        assertTrue(error.hasField("incrementFee",
                "The increment fee must be a positive number greater than 0.0!"));
        assertTrue(error.hasField("incrementTime",
                "The increment time must be a positive integer greater than 0!"));
        assertTrue(error.hasField("maxIncrementTime",
                "The max increment time must be a positive integer greater than 0!"));
    }
}
