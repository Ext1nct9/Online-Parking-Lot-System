package ca.mcgill.ecse321.opls.service.auth;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.dto.auth.AccessToken;
import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

/**
 * Test the OAuthGrantService class.
 */
@ExtendWith(MockitoExtension.class)
public class TestOAuthGrantService {

	@Mock
	private UserAccountAuthService userService;

	@Mock
	private OAuthSessionService sessionService;

	@InjectMocks
	private OAuthGrantService grantService;

	private void setupMocks(OAuthClient client, UserAccount user) {
		var session = new OAuthClientSession();
		session.setClient(client);
		session.setUserAccount(user);
		session.setExpiry(10);

		when(sessionService.newSession(any(client.getClass()),
				any(user.getClass()), anyLong()))
						.thenAnswer((InvocationOnMock invocation) -> session);

		when(userService.getUserClaims(user))
				.thenAnswer((InvocationOnMock invocation) -> Arrays
						.asList(OAuthClaim.ADMIN));
	}

	/**
	 * Test a password request.
	 */
	@Test
	public void testFulfillPasswordGrant() {
		var requestingClient = new OAuthClient("Name", "name", "secret");

		final String username = "username";
		final String password = "password";
		var userAccount = new UserAccount();
		userAccount.overrideId(150);

		setupMocks(requestingClient, userAccount);
		when(userService.authenticateUser(username, password))
				.thenAnswer((InvocationOnMock invocation) -> userAccount);

		// test valid request
		var tokenToFill = new AccessToken();
		var request = TokenRequestDto.passwordRequest(username, password);
		var session = grantService.populateAccessToken(requestingClient,
				request, tokenToFill);
		assertNotNull(session);
		assertEquals(requestingClient.getClientId(),
				session.getClient().getClientId());
		assertEquals(requestingClient.getClientId(), tokenToFill.oauthClientId);
		assertEquals(userAccount.getId(), session.getUserAccount().getId());
		assertEquals(userAccount.getId(), tokenToFill.userAccountId);
		assertTrue(tokenToFill.isRegistered);
		assertTrue(tokenToFill.hasClaim(OAuthClaim.ADMIN));

		// test invalid request
		request.username = null;
		request.password = null;
		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Missing username or password.",
				() -> grantService.populateAccessToken(requestingClient,
						request, tokenToFill));
	}

	/**
	 * Test a client credentials request.
	 */
	@Test
	public void testFulfillClientCredentialsGrant() {
		var requestingClient = new OAuthClient("Name", "name", "secret");

		// test valid request
		var tokenToFill = new AccessToken();
		var request = TokenRequestDto.clientCredentialsRequest();
		var session = grantService.populateAccessToken(requestingClient,
				request, tokenToFill);
		assertNull(session);
		assertEquals(requestingClient.getClientId(), tokenToFill.oauthClientId);
		assertFalse(tokenToFill.isRegistered);
		assertTrue(tokenToFill.hasClaim(OAuthClaim.CUSTOMER));
	}

	/**
	 * Test a refresh token request.
	 */
	@Test
	public void testRefreshTokenGrant() {
		var requestingClient = new OAuthClient("Name", "name", "secret");

		var userAccount = new UserAccount();
		userAccount.overrideId(150);

		var session = new OAuthClientSession();
		session.setClient(requestingClient);
		session.setRandomRefreshToken();
		session.setUserAccount(userAccount);
		session.setExpiry(new Date().getTime() + 10000000);

		setupMocks(requestingClient, userAccount);
		when(sessionService.validateRefreshToken(session.getRefreshToken(),
				requestingClient))
						.thenAnswer((InvocationOnMock invocation) -> session);

		// test valid request
		var tokenToFill = new AccessToken();
		var request = TokenRequestDto
				.refreshTokenRequest(session.getRefreshToken());
		var newSession = grantService.populateAccessToken(requestingClient,
				request, tokenToFill);
		assertNotNull(session);
		assertEquals(requestingClient.getClientId(),
				newSession.getClient().getClientId());
		assertEquals(requestingClient.getClientId(), tokenToFill.oauthClientId);
		assertEquals(userAccount.getId(), newSession.getUserAccount().getId());
		assertEquals(userAccount.getId(), tokenToFill.userAccountId);
		assertTrue(tokenToFill.isRegistered);
		assertTrue(tokenToFill.hasClaim(OAuthClaim.ADMIN));

		// test invalid request
		request.refreshToken = null;
		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Missing refresh token.",
				() -> grantService.populateAccessToken(requestingClient,
						request, tokenToFill));

	}

}
