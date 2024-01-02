package ca.mcgill.ecse321.opls.service.auth;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
public class TestUserAccountAuthService {

	@Mock
	private UserAccountRepository userAccountRepository;

	@Mock
	private UserAccountClaimRepository claimRepository;

	@InjectMocks
	private UserAccountAuthService service;

	private UserAccount createUser(String username, String password) {
		// create user
		var userAccount = new UserAccount();
		userAccount.setFirstName("Admin");
		userAccount.setLastName("Admin");
		userAccount.setUsername(username);
		try {
			userAccount.setPassword(password);
		} catch (Exception e) {
			fail(e);
		}
		userAccount.setSecurityAnswer("What is the code?", "123");

		lenient()
				.when(userAccountRepository.findUserAccountByUsername(username))
				.thenAnswer((InvocationOnMock invocation) -> userAccount);

		return userAccount;
	}

	@Test
	public void testAuthenticateUser() {
		final String username = "john";
		final String password = "pw123";
		var user = createUser(username, password);

		// test valid login
		var returnedAccount = service.authenticateUser(username, password);
		assertNotNull(returnedAccount);
		assertEquals(username, returnedAccount.getUsername());
		assertEquals(user.getLastName(), returnedAccount.getLastName());

		// test incorrect login
		final String incorrectPassword = "not_pass";

		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Incorrect username or password.",
				() -> service.authenticateUser(username, incorrectPassword));

		final String incorrectUsername = "not_john";
		assertThrowsApiException(HttpStatus.UNAUTHORIZED, "invalid_grant",
				"Incorrect username or password.",
				() -> service.authenticateUser(incorrectUsername, incorrectPassword));
	}

	@Test
	public void testGetClaims() {
		final String username = "john";
		final String password = "pw123";
		var user = createUser(username, password);

		var claims = new ArrayList<OAuthClaim>();
		claims.add(OAuthClaim.ADMIN);
		claims.add(OAuthClaim.CUSTOMER);

		for (var claim : claims) {
			claimRepository.save(user.addUserAccountClaim(claim));
		}

		lenient().when(claimRepository.getUserClaims(user))
				.thenAnswer((InvocationOnMock invocation) -> claims);

		var returnedClaims = service.getUserClaims(user);
		assertEquals(2, returnedClaims.size());
		assertTrue(returnedClaims.contains(OAuthClaim.ADMIN));
		assertTrue(returnedClaims.contains(OAuthClaim.CUSTOMER));
	}

}
