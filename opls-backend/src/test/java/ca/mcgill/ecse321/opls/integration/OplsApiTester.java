package ca.mcgill.ecse321.opls.integration;

import static ca.mcgill.ecse321.opls.TestUtils.validateErrorObj;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.auth.ApiClient;
import ca.mcgill.ecse321.opls.dto.OplsApiErrorResponseDto;
import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.OAuthClientRepository;
import ca.mcgill.ecse321.opls.repository.OAuthClientSessionRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;

/**
 * Parent class to provide automatic authentication for the integration test
 * classes.
 */
public abstract class OplsApiTester {

	/**
	 * Whether to authorize the OAuthClient object before each test. Override in
	 * subclasses to prevent this from happening (authorization testing).
	 */
	protected boolean AUTO_AUTHORIZE = true;

	/** Port the API is running on. */
	@LocalServerPort
	private int localPort;

	@Autowired
	private UserAccountRepository userAccountRepository;
	private UserAccount userAccount;
	private static final String TESTER_USERNAME = "tester";
	private static final String TESTER_PASSWORD = "pw1234567";
	private static final String TESTER_SEC_Q = "What is the code?";
	private static final String TESTER_SEC_ANS = "12345";
	private static final String TESTER_FIRST_NAME = "First";
	private static final String TESTER_LAST_NAME = "Last";

	@Autowired
	private UserAccountClaimRepository userAccountClaimRepository;
	private Set<OAuthClaim> defaultUserClaims = new HashSet<OAuthClaim>();

	@Autowired
	private RestTemplateBuilder builder;
	protected ApiClient apiClient;

	@Autowired
	private OAuthClientRepository clientRepository;
	private OAuthClient testerClient;
	private static final String TESTER_CLIENT_ID = "tester";
	private static final String TESTER_CLIENT_SECRET = "secret";

	@Autowired
	private OAuthClientSessionRepository sessionRepository;

	/** Create the API client. */
	@PostConstruct
	public void initialize() {
		apiClient = new ApiClient("http://localhost:" + localPort, builder,
				TESTER_CLIENT_ID, TESTER_CLIENT_SECRET);
	}

	/**
	 * Subclass initialization method to specify how the tester must be
	 * authenticated. Must be annotated with @PostConstruct.
	 */
	public abstract void specifyAuthentication();

	/**
	 * Add an OAuthClaim to give the tester account.
	 */
	protected void addDefaultClaim(OAuthClaim claim) {
		this.defaultUserClaims.add(claim);
	}

	/**
	 * Subclass initialization method to setup test data for the created user
	 * account before each test. This can create entries associated with the
	 * user using getUserAccount(). Must be annotated with @BeforeEach. Leave
	 * empty if no test data needs to be setup for every test.
	 */
	public abstract void setupTestData();

	/**
	 * Create the authorization entities needed to make API requests by the
	 * tester. This creates an OAuthClient entry along with a default
	 * UserAccount if the AUTO_AUTHORIZE flag is set to true.
	 */
	@BeforeEach
	public void registerTester() throws Exception {
		// find or create client
		testerClient = clientRepository
				.findOAuthClientByClientId(TESTER_CLIENT_ID);
		if (testerClient == null) {
			testerClient = clientRepository.save(new OAuthClient(
					"Tester Client", TESTER_CLIENT_ID, TESTER_CLIENT_SECRET));
		}

		// authorize the client if needed
		if (AUTO_AUTHORIZE) {
			// find or create tester user
			userAccount = userAccountRepository
					.save(OplsStartupService.defaultUser(TESTER_USERNAME,
							TESTER_PASSWORD, TESTER_SEC_Q, TESTER_SEC_ANS,
							TESTER_FIRST_NAME, TESTER_LAST_NAME));

			for (var c : defaultUserClaims) {
				userAccountClaimRepository
						.save(userAccount.addUserAccountClaim(c));
			}

			var tokenReq = TokenRequestDto.passwordRequest(TESTER_USERNAME,
					TESTER_PASSWORD);
			this.apiClient.authenticate(tokenReq);
		} else {
			userAccount = null;
		}
	}

	/** Clear any created authorization entities. */
	@AfterEach
	public void superClearDatabase() {
		sessionRepository.deleteAll();
		clientRepository.deleteAll();
		userAccountClaimRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	/** Get the OAuthClient the tester is using. */
	protected OAuthClient getTesterClient() {
		return testerClient;
	}

	/** Get the UserAccount the tester is logged in with. */
	protected UserAccount getUserAccount() {
		return userAccount;
	}

	/**
	 * Send an HTTP request with a request body.
	 * 
	 * @param <T>
	 *            The request body type.
	 * @param <U>
	 *            The expected response type.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The target endpoint.
	 * @param body
	 *            The request body.
	 * @param responseClass
	 *            The expected response type.
	 * @param expectedStatus
	 *            The expected response status code.
	 * @return The response entity.
	 */
	protected <T, U> ResponseEntity<U> exchange(HttpMethod httpMethod,
			String endpoint, T body, Class<U> responseType,
			HttpStatus expectedStatus) {
		var resp = apiClient.exchange(httpMethod, endpoint, body, responseType);
		assertNotNull(resp);
		if (expectedStatus != HttpStatus.NO_CONTENT) {
			assertNotNull(resp.getBody());
		}
		if (expectedStatus != null) {
			assertEquals(expectedStatus, resp.getStatusCode());
		}
		return resp;
	}

	/**
	 * Send an HTTP request without a request body.
	 * 
	 * @param <T>
	 *            The request body type.
	 * @param <U>
	 *            The expected response type.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The target endpoint.
	 * @param body
	 *            The request body.
	 * @param responseClass
	 *            The expected response type.
	 * @param expectedStatus
	 *            The expected response status code.
	 * @return The response entity.
	 */
	protected <U> ResponseEntity<U> exchange(HttpMethod httpMethod,
			String endpoint, Class<U> responseType, HttpStatus expectedStatus) {
		var resp = apiClient.exchange(httpMethod, endpoint, responseType);
		assertNotNull(resp);
		if (expectedStatus != HttpStatus.NO_CONTENT) {
			assertNotNull(resp.getBody());
		}
		if (expectedStatus != null) {
			assertEquals(expectedStatus, resp.getStatusCode());
		}
		return resp;
	}

	/**
	 * Send an HTTP request and ensure the response is an error.
	 * 
	 * @param <T>
	 *            The request body type.
	 * @param expectedStatus
	 *            The expected response status.
	 * @param expectedError
	 *            The expected error field.
	 * @param expectedMessage
	 *            The expected message field.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The request endpoint.
	 * @param body
	 *            The request body.
	 * @param headers
	 *            The request headers.
	 * @return The error response.
	 */
	protected <T> OplsApiErrorResponseDto assertReturnsError(
			HttpStatusCode expectedStatus, String expectedError,
			String expectedMessage, HttpMethod method, String endpoint, T body,
			HttpHeaders headers) {
		ResponseEntity<OplsApiErrorResponseDto> resp;
		if (headers == null) {
			resp = this.apiClient.exchange(method, endpoint, body,
					OplsApiErrorResponseDto.class);
		} else {
			resp = this.apiClient.exchange(method, endpoint, body, headers,
					OplsApiErrorResponseDto.class);
		}
		assertEquals(expectedStatus, resp.getStatusCode());
		validateErrorObj(expectedError, expectedMessage, resp.getBody());
		return resp.getBody();
	}

	/**
	 * Send an HTTP request and ensure the response is an error.
	 * 
	 * @param expectedStatus
	 *            The expected response status.
	 * @param expectedError
	 *            The expected error field.
	 * @param expectedMessage
	 *            The expected message field.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The request endpoint.
	 * @param headers
	 *            The request headers.
	 * @return The error response.
	 */
	protected OplsApiErrorResponseDto assertReturnsError(
			HttpStatusCode expectedStatus, String expectedError,
			String expectedMessage, HttpMethod method, String endpoint,
			HttpHeaders headers) {
		ResponseEntity<OplsApiErrorResponseDto> resp;
		if (headers == null) {
			resp = this.apiClient.exchange(method, endpoint,
					OplsApiErrorResponseDto.class);
		} else {
			resp = this.apiClient.exchange(method, endpoint, headers,
					OplsApiErrorResponseDto.class);
		}

		assertEquals(expectedStatus, resp.getStatusCode(),
				resp.getBody().error);
		validateErrorObj(expectedError, expectedMessage, resp.getBody());
		return resp.getBody();
	}

}
