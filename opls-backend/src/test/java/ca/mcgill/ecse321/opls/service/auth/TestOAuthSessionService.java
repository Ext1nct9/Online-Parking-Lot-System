package ca.mcgill.ecse321.opls.service.auth;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.OAuthClientSessionRepository;

@ExtendWith(MockitoExtension.class)
public class TestOAuthSessionService {

	@Mock
	private OAuthClientSessionRepository sessionRepository;

	@InjectMocks
	private OAuthSessionService service;

	/**
	 * Test validating a refresh token request.
	 */
	@Test
	public void testValidateRefreshToken() {
		var client1 = new OAuthClient();
		client1.setClientId("client1");
		var client2 = new OAuthClient();
		client2.setClientId("client2");

		final long expiry = new Date().getTime() + (long) 10000000;

		var session = new OAuthClientSession();
		session.setRandomRefreshToken();
		session.setClient(client1);
		session.setExpiry(expiry);
		lenient()
				.when(sessionRepository
						.findSessionByRefreshToken(session.getRefreshToken()))
				.thenAnswer((InvocationOnMock invocation) -> session);

		// test valid token
		var returnedSession = service
				.validateRefreshToken(session.getRefreshToken(), client1);
		assertNotNull(returnedSession);
		assertEquals(expiry, returnedSession.getExpiry());
		assertEquals(client1.getUuid(), returnedSession.getClient().getUuid());

		// test missing session
		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Invalid refresh token.",
				() -> service.validateRefreshToken("token_wrong", client1));

		// test mismatch client
		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Invalid refresh token.",
				() -> service.validateRefreshToken(session.getRefreshToken(),
						client2));

		// test expired session
		session.setExpiry(100);
		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Expired refresh token.",
				() -> service.validateRefreshToken(session.getRefreshToken(),
						client1));
	}

	/**
	 * Test creating a new session.
	 */
	@Test
	public void testNewSession() {
		// return the session with no modification
		lenient().when(sessionRepository.save(any(OAuthClientSession.class)))
				.thenAnswer((InvocationOnMock invocation) -> invocation
						.getArgument(0));

		final OAuthClient client = new OAuthClient();
		client.setClientId("my_client");
		final UserAccount user = new UserAccount();
		user.setUsername("hello");
		final long expiry = 120;

		var returnedSession = service.newSession(client, user, expiry);
		assertNotNull(returnedSession);
		assertEquals(expiry, returnedSession.getExpiry());

		verify(sessionRepository, times(1)).save(returnedSession);
	}

}
