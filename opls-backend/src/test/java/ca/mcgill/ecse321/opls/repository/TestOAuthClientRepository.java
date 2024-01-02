package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.auth.OAuthClient;

@SpringBootTest
public class TestOAuthClientRepository {

	@Autowired
	private OAuthClientRepository clientRepository;
	
	@AfterEach
	public void clearDatabase() {
		clientRepository.deleteAll();
	}
	
	/**
	 * Test creating and reading OAuthClient entries.
	 */
	@Test
	public void testCreateRead() {
		var client = clientRepository.save(new OAuthClient("TestClient", "testerid", "secret"));
		
		var res = clientRepository.findOAuthClientByUuid(client.getUuid());
		assertNotNull(res);
		assertEquals(client.getClientId(), res.getClientId());
		
		res = clientRepository.findOAuthClientByClientId(client.getClientId());
		assertNotNull(res);
		assertEquals(client.getName(), res.getName());
		assertEquals(client.getSecret(), res.getSecret());
		
		res = clientRepository.findOAuthClientByClientIdAndSecret(client.getClientId(), client.getSecret());
		assertNotNull(res);
		assertEquals(client.getClientId(), res.getClientId());
		assertEquals(client.getId(), res.getId());
		assertEquals(client.getUuid(), res.getUuid());
		assertEquals(client.getName(), res.getName());
	}
	
	/**
	 * Ensure the database enforces unique string ID values.
	 */
	@Test
	public void testUnique() {
		var client1 = clientRepository.save(new OAuthClient("TestClient", "testid", "secret"));
		var result = clientRepository.findOAuthClientByClientId("testid");
		assertNotNull(result);
		
		// try to save the repeat clientId
		try {
			clientRepository.save(new OAuthClient("TestClient 2", "testid", "secret2"));
			fail("Able to create multiple clients with the same client id.");
		} catch (Exception e) {
		}
		
		// ensure first client still saved
		result = clientRepository.findOAuthClientByClientId("testid");
		assertNotNull(result);
		assertEquals(client1.getName(), result.getName());
	}
	
}
