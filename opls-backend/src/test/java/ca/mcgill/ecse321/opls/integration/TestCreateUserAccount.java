package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.dto.account.UserAccountRequestDto;
import ca.mcgill.ecse321.opls.dto.account.UserAccountResponseDto;
import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestCreateUserAccount extends OplsApiTester {

	private static final String username = "Ordinateur",
			password = "Password123", firstName = "Comp", lastName = "Youter",
			securityQuestion = "Multivac, what do you yourself want more than anything else?",
			securityQuestionAnswer = "Patate frites";

	/**
	 * We do not auto authorize because we want to test creating an account.
	 */
	@Override
	public void specifyAuthentication() {
		this.AUTO_AUTHORIZE = false;
		this.addDefaultClaim(OAuthClaim.ADMIN);
        this.addDefaultClaim(OAuthClaim.EMPLOYEE);
	}

	@Override
	public void setupTestData() {
		
	}

	/**
	 * Test POST /account request.
	 */
	@Test
	public void testCreateAccount() {
		// authenticate the client
		this.apiClient.authenticate(TokenRequestDto.clientCredentialsRequest());

		// test invalid request
		var request = new UserAccountRequestDto();
		request.username = "asdf";
		request.password = "asdf";
		request.firstName = "";
		request.lastName = "";
		request.securityQuestion = "";
		request.securityAnswer = "";
		var response = this.assertReturnsError(HttpStatus.BAD_REQUEST,
				"Invalid request body.", null, HttpMethod.POST, "/account",
				request, null);
		assertTrue(response.hasField("password", "The field must be at least 8 characters!"));
		assertTrue(response.hasField("username", "The field must be at least 6 characters!"));
		assertTrue(response.hasField("firstName", "The field must be at least 1 character!"));
		assertTrue(response.hasField("lastName", "The field must be at least 1 character!"));
		assertTrue(response.hasField("securityQuestion", "The field must be at least 1 character!"));
		assertTrue(response.hasField("securityAnswer", "The field must be at least 1 character!"));

		// make a user account request
		request = new UserAccountRequestDto(username, firstName, lastName,
				password, securityQuestion, securityQuestionAnswer);
		var postResponse = this.exchange(HttpMethod.POST, "/account", request,
				UserAccountResponseDto.class, HttpStatus.OK).getBody();
		assertNotNull(postResponse.uuid);
		assertEquals(firstName, postResponse.firstname);
		assertEquals(lastName, postResponse.lastname);
		assertEquals(securityQuestion, postResponse.securityQuestion);

		// login with the new account
		this.apiClient.authenticate(
				TokenRequestDto.passwordRequest(username, password));
		var endpoint = "/account/"+postResponse.uuid;
		var getResponse = this.exchange(HttpMethod.GET, endpoint,
				UserAccountResponseDto.class, HttpStatus.OK).getBody();
		assertEquals(postResponse.uuid, getResponse.uuid);
		assertEquals(firstName, getResponse.firstname);
		assertEquals(lastName, getResponse.lastname);
		assertEquals(securityQuestion, getResponse.securityQuestion);
	}

}
