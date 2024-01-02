package ca.mcgill.ecse321.opls.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.opls.model.auth.OAuthClient;

/**
 * Repository to interface with the oauth_client table.
 */
public interface OAuthClientRepository extends CrudRepository<OAuthClient, Integer> {
	
	/** Find an OAuthClient entry by its uuid. */
	OAuthClient findOAuthClientByUuid(UUID uuid);
	
	/** Find an OAuthClient entry by its string id value. */
	OAuthClient findOAuthClientByClientId(String clientId);
	
	/** Find an OAuthClient entry by its string id and secret. */
	@Query(value = "SELECT * FROM oauth_client WHERE client_id = :clientId AND secret = :secret", nativeQuery = true)
	OAuthClient findOAuthClientByClientIdAndSecret(@Param("clientId") String clientId, @Param("secret") String secret);
	
}
