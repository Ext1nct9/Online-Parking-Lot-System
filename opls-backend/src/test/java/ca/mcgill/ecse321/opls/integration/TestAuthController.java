package ca.mcgill.ecse321.opls.integration;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.tokenRequestHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.auth.OAuthHelper;
import ca.mcgill.ecse321.opls.dto.auth.AccessToken;
import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.dto.auth.TokenResponseDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;
import ca.mcgill.ecse321.opls.repository.OAuthClientSessionRepository;
import ca.mcgill.ecse321.opls.repository.TestUserAccountRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestAuthController extends OplsApiTester {

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private UserAccountClaimRepository userAccountClaimRepository;

	@Autowired
	private OAuthClientSessionRepository sessionRepository;

	/**
	 * Since we are testing the authorization, we do not want the tester to
	 * auto-authorize our client.
	 */
	@Override
	@PostConstruct
	public void specifyAuthentication() {
		this.AUTO_AUTHORIZE = false;
	}

	@Override
	@BeforeEach
	public void setupTestData() {
	}

	@AfterEach
	public void clearDatabase() {
		sessionRepository.deleteAll();
		userAccountClaimRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	private TokenResponseDto submitSuccessfulTokenRequest(
			TokenRequestDto request, String clientId, String clientSecret,
			HttpStatusCode expectedStatus) {
		var reqObj = request.toRequestMap();

		var resp = this.apiClient.exchange(HttpMethod.POST, "/token", reqObj,
				tokenRequestHeaders(clientId, clientSecret),
				TokenResponseDto.class);

		assertEquals(expectedStatus, resp.getStatusCode());

		var body = resp.getBody();
		assertNotNull(body);
		return body;
	}

	private void submitFailedTokenRequest(TokenRequestDto request,
			String clientId, String clientSecret, HttpStatusCode expectedStatus,
			String expectedError, String expectedMessage) {
		var reqObj = request.toRequestMap();
		var headers = tokenRequestHeaders(clientId, clientSecret);

		assertReturnsError(expectedStatus, expectedError, expectedMessage,
				HttpMethod.POST, "/token", reqObj, headers);
	}

	private AccessToken decodeAccessToken(TokenResponseDto response) {
		String bearerToken = response.tokenType + " " + response.accessToken;
		return AccessTokenHelper.parseAccessToken(bearerToken, false, null);
	}

	/**
	 * Test client credentials flow.
	 */
	@Test
	public void testClientCredentials() {
		var clientId = getTesterClient().getClientId();
		var clientSecret = getTesterClient().getSecret();

		// make valid request
		var tokenReq = TokenRequestDto.clientCredentialsRequest();
		var tokenResp = submitSuccessfulTokenRequest(tokenReq, clientId,
				clientSecret, HttpStatus.OK);

		// validate output
		assertNotNull(tokenResp);
		assertNotNull(tokenResp.accessToken);
		assertEquals(OAuthHelper.OAUTH_ACCESS_TOKEN_EXPIRY,
				tokenResp.expiresIn);
		assertEquals("bearer", tokenResp.tokenType);
		assertNull(tokenResp.refreshToken);

		// validate token
		var token = decodeAccessToken(tokenResp);
		assertEquals(clientId, token.oauthClientId);
		assertFalse(token.isRegistered);
		assertTrue(token.hasClaim(OAuthClaim.CUSTOMER));

		// make invalid request
		submitFailedTokenRequest(tokenReq, clientId + "adsf", clientSecret,
				HttpStatus.BAD_REQUEST, "invalid_client", "Client not found.");
	}

	/**
	 * Test login flow with a username and password.
	 */
	@Test
	public void testPassword() {
		var clientId = getTesterClient().getClientId();
		var clientSecret = getTesterClient().getSecret();

		// create user
		final String username = "john";
		final String password = "pass123";
		var user = userAccountRepository
				.save(TestUserAccountRepository.newAccount(username, "first",
						"last", password, "question", "answer"));
		userAccountClaimRepository
				.save(user.addUserAccountClaim(OAuthClaim.ADMIN));

		// make valid login request
		var tokenReq = TokenRequestDto.passwordRequest(username, password);
		var tokenResp = submitSuccessfulTokenRequest(tokenReq, clientId,
				clientSecret, HttpStatus.OK);

		// validate output
		assertNotNull(tokenResp.accessToken);
		assertNotNull(tokenResp.refreshToken);
		var token = decodeAccessToken(tokenResp);
		assertEquals(user.getId(), token.userAccountId);
		assertEquals(clientId, token.oauthClientId);
		assertTrue(token.isRegistered);
		assertTrue(token.hasClaim(OAuthClaim.ADMIN));

		// make invalid request
		tokenReq.password = "asdf";
		submitFailedTokenRequest(tokenReq, clientId, clientSecret,
				HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Incorrect username or password.");
	}

	/**
	 * Test refresh token flow.
	 */
	@Test
	public void testRefreshToken() {
		var client = getTesterClient();
		var clientId = client.getClientId();
		var clientSecret = client.getSecret();

		// create user
		final String username = "john";
		final String password = "pass123";
		var user = userAccountRepository
				.save(TestUserAccountRepository.newAccount(username, "first",
						"last", password, "question", "answer"));
		userAccountClaimRepository
				.save(user.addUserAccountClaim(OAuthClaim.ADMIN));

		// create session
		var session = new OAuthClientSession();
		session.setRandomRefreshToken();
		session.setExpiry(new Date().getTime() + 10000000);
		session.setClient(client);
		session.setUserAccount(user);
		session = sessionRepository.save(session);

		// make valid refresh token request
		var tokenReq = TokenRequestDto
				.refreshTokenRequest(session.getRefreshToken());
		var tokenResp = submitSuccessfulTokenRequest(tokenReq, clientId,
				clientSecret, HttpStatus.OK);

		// validate output
		assertNotNull(tokenResp.accessToken);
		assertNotNull(tokenResp.refreshToken);
		var token = decodeAccessToken(tokenResp);
		assertEquals(user.getId(), token.userAccountId);
		assertEquals(clientId, token.oauthClientId);
		assertTrue(token.isRegistered);
		assertTrue(token.hasClaim(OAuthClaim.ADMIN));

		// ensure cannot reuse previous refresh token
		submitFailedTokenRequest(tokenReq, clientId, clientSecret,
				HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Invalid refresh token.");

		// request again on the refreshed session
		tokenReq.refreshToken = tokenResp.refreshToken;
		tokenResp = submitSuccessfulTokenRequest(tokenReq, clientId,
				clientSecret, HttpStatus.OK);
		assertNotNull(tokenResp.accessToken);
		assertNotNull(tokenResp.refreshToken);
		token = decodeAccessToken(tokenResp);
		assertEquals(user.getId(), token.userAccountId);
		assertEquals(clientId, token.oauthClientId);
		assertTrue(token.isRegistered);
		assertTrue(token.hasClaim(OAuthClaim.ADMIN));

		// request on an expired session
		session = sessionRepository
				.findSessionByRefreshToken(tokenResp.refreshToken);
		session.setExpiry(100);
		session = sessionRepository.save(session);
		tokenReq.refreshToken = tokenResp.refreshToken;
		submitFailedTokenRequest(tokenReq, clientId, clientSecret,
				HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Expired refresh token.");

		// make invalid request
		tokenReq.refreshToken = "asdf";
		submitFailedTokenRequest(tokenReq, clientId, clientSecret,
				HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Invalid refresh token.");
	}

}
