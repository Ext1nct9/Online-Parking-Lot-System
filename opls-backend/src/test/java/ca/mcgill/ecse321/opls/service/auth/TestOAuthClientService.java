package ca.mcgill.ecse321.opls.service.auth;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static ca.mcgill.ecse321.opls.auth.OAuthHelper.generateBasicHeader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.repository.OAuthClientRepository;

/**
 * Test the OAuthClientService class.
 */
@ExtendWith(MockitoExtension.class)
public class TestOAuthClientService {

	@Mock
	private OAuthClientRepository clientRepository;

	@InjectMocks
	private OAuthClientService service;

	/**
	 * Test parsing the basic authorization header.
	 */
	@Test
	public void testParseAuthorizationHeader() {
		// create client
		final String clientId = "test_client";
		final String clientSecret = "test_secret";
		var client = new OAuthClient();
		client.setClientId(clientId);
		client.setName("Tester");
		client.setSecret(clientSecret);

		lenient()
				.when(clientRepository.findOAuthClientByClientIdAndSecret(
						clientId, clientSecret))
				.thenAnswer((InvocationOnMock invocation) -> client);

		// test valid header
		var header = generateBasicHeader(clientId, clientSecret);
		var returnedClient = service
				.getOAuthClientFromAuthorizationHeader(header);
		assertNotNull(returnedClient);
		assertEquals(client.getName(), returnedClient.getName());
		assertEquals(client.getUuid(), returnedClient.getUuid());

		// test client not found
		var header2 = generateBasicHeader(clientId, "asdf");
		assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_client",
				"Client not found.",
				() -> service.getOAuthClientFromAuthorizationHeader(header2));

		// test malformed header
		var header3 = generateBasicHeader(clientId, clientSecret) + "asdf";
		assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
				"Malformed Authorization header.",
				() -> service.getOAuthClientFromAuthorizationHeader(header3));
	}

}
