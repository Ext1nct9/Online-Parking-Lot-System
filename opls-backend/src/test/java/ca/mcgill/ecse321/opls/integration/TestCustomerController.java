package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.dto.CustomerDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestCustomerController extends OplsApiTester {

	@Override
	public void specifyAuthentication() {
		this.addDefaultClaim(OAuthClaim.ADMIN);
	}

	@Override
	@BeforeEach
	public void setupTestData() {
	}

	private void assertDtoEquals(CustomerDto expected, CustomerDto actual) {
		assertNotNull(actual);
		assertEquals(expected.billingAccountId, actual.billingAccountId);
		assertEquals(expected.savedLicensePlate, actual.savedLicensePlate);
	}

	/**
	 * Test a customer entity.
	 */
	@Test
	public void testCRUDCustomer() {
		var request = new CustomerDto();
		request.billingAccountId = "1234123412341234";

		// create
		var postResponse = this.exchange(HttpMethod.POST, "/customer", request,
				CustomerDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(request, postResponse);
		
		// re-authorize with new customer claims
		this.apiClient.refreshAuthentication();

		// get
		var getResponse = this.exchange(HttpMethod.GET, "/customer",
				CustomerDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(postResponse, getResponse);

		// update
		request.billingAccountId = null;
		request.savedLicensePlate = "ABCD";
		var patchResponse = this.exchange(HttpMethod.PATCH, "/customer",
				request, CustomerDto.class, HttpStatus.OK).getBody();
		assertEquals(postResponse.billingAccountId,
				patchResponse.billingAccountId);
		assertEquals(request.savedLicensePlate,
				patchResponse.savedLicensePlate);

		// delete
		var deleteResponse = this.exchange(HttpMethod.DELETE, "/customer",
				CustomerDto.class, HttpStatus.OK).getBody();
		assertDtoEquals(patchResponse, deleteResponse);

		// ensure cannot get
		this.assertReturnsError(HttpStatus.NOT_FOUND, "Customer not found.",
				null, HttpMethod.GET, "/customer", null);
	}

}
