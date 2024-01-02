package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.auth.OAuthClient;

@SpringBootTest
public class TestOAuthClientSessionRepository {

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private OAuthClientRepository clientRepository;

	@Autowired
	private OAuthClientSessionRepository sessionRepository;

	@AfterEach
	public void clearDatabase() {
		sessionRepository.deleteAll();
		clientRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	@Test
	public void testCreateReadDelete() {
		var client = clientRepository
				.save(new OAuthClient("TestClient", "testid", "secret"));
		var session = client.newSession();
		session.setExpiry(1245);
		session = sessionRepository.save(session);
		assertEquals(client.getId(), session.getClient().getId());

		// get session
		var result = sessionRepository
				.findSessionByRefreshToken(session.getRefreshToken());
		assertNotNull(result);
		assertEquals(session.getRefreshToken(), result.getRefreshToken());
		assertEquals(session.getExpiry(), result.getExpiry());
		assertEquals(client.getId(), result.getClient().getId());

		// delete session
		sessionRepository.delete(session);
		result = sessionRepository
				.findSessionByRefreshToken(session.getRefreshToken());
		assertNull(result);
	}

	/**
	 * Test a session linked to a UserAccount.
	 */
	@Test
	public void testUserAccountSession() {
		var user = userAccountRepository.save(TestUserAccountRepository
				.newAccount("john", "John", "Appleseed", "password",
						"Where are you happiest in the world?",
						"I don't know."));
		var client = clientRepository
				.save(new OAuthClient("TestClient", "testid", "secret"));
		
		var session = client.newSession();
		session.setExpiry(1245);
		session.setUserAccount(user);
		session = sessionRepository.save(session);

		// get session
		var result = sessionRepository
				.findSessionByRefreshToken(session.getRefreshToken());
		assertNotNull(result);
		assertNotNull(result.getUserAccount());
		assertEquals(user.getId(), result.getUserAccount().getId());
		assertEquals(client.getId(), result.getClient().getId());
		
		// get sessions
		result = null;
		for (var sessionResult : sessionRepository.findUserSessions(user.getId())) {
			if (sessionResult.getRefreshToken().equals(session.getRefreshToken())) {
				result = sessionResult;
				break;
			}
		}
		assertNotNull(result);
		
		// get sessions
		result = null;
		for (var sessionResult : sessionRepository.findOAuthClientSessionByUserAccount(user)) {
			if (sessionResult.getRefreshToken().equals(session.getRefreshToken())) {
				result = sessionResult;
				break;
			}
		}
		assertNotNull(result);
	}

}
