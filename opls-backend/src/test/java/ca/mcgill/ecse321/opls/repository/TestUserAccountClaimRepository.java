package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

@SpringBootTest
public class TestUserAccountClaimRepository {
	
	@Autowired
	private UserAccountRepository userAccountRepository;
	
	@Autowired
	private UserAccountClaimRepository userAccountClaimRepository;
	
	@AfterEach
	public void clearDatabase() {
		userAccountClaimRepository.deleteAll();
		userAccountRepository.deleteAll();
	}
	
	private void assertExists(Iterable<OAuthClaim> claims, UserAccount userAccount, OAuthClaim targetClaim) {
		for (var claim : claims) {
			if (claim == targetClaim) {
				return;
			}
		}
		fail("Claim not found");
	}
	
	private void assertAbsent(Iterable<OAuthClaim> claims, UserAccount userAccount, OAuthClaim targetClaim) {
		for (var claim : claims) {
			if (claim == targetClaim) {
				fail("UserAccountClaim is present");
			}
		}
	}
	
	/**
	 * Test creating, reading, and deleting claims.
	 */
	@Test
	public void testCRUD() {
		// create user with claims
		var user = userAccountRepository.save(TestUserAccountRepository.newAccount("john", "John", "Appleseed", "pw", "q", "a"));
		userAccountClaimRepository.save(user.addUserAccountClaim(OAuthClaim.ADMIN));
		var employeeClaim = userAccountClaimRepository.save(user.addUserAccountClaim(OAuthClaim.EMPLOYEE));
		
		assertFalse(userAccountClaimRepository.userHasClaim(user, OAuthClaim.NONE));
		assertTrue(userAccountClaimRepository.userHasClaim(user, OAuthClaim.ADMIN));
		assertTrue(userAccountClaimRepository.userHasClaim(user, OAuthClaim.EMPLOYEE));
		assertFalse(userAccountClaimRepository.userHasClaim(user, OAuthClaim.CUSTOMER));

		// get all claims through user object
		var result = userAccountRepository.findUserAccountByUsername("john");
		var claims = userAccountClaimRepository.getUserClaims(result);
		assertAbsent(claims, user, OAuthClaim.NONE);
		assertExists(claims, user, OAuthClaim.ADMIN);
		assertExists(claims, user, OAuthClaim.EMPLOYEE);
		assertAbsent(claims, user, OAuthClaim.CUSTOMER);
		
		// delete claim
		userAccountClaimRepository.delete(employeeClaim);
		result = userAccountRepository.findUserAccountByUsername("john");
		claims = userAccountClaimRepository.getUserClaims(result);
		assertAbsent(claims, user, OAuthClaim.NONE);
		assertExists(claims, user, OAuthClaim.ADMIN);
		assertAbsent(claims, user, OAuthClaim.EMPLOYEE);
		assertAbsent(claims, user, OAuthClaim.CUSTOMER);
	}
	
}
