package ca.mcgill.ecse321.opls.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

/**
 * Repository to interface with the oauth_client_session table.
 */
public interface OAuthClientSessionRepository extends CrudRepository<OAuthClientSession, String> {

	/** Find a session by its identifying refresh token. */
	OAuthClientSession findSessionByRefreshToken(String refreshToken);
	
	/** Find all user sessions associated with a user account. */
	@Query(value = "SELECT * FROM oauth_client_session WHERE user_account_id = :userAccountId", nativeQuery = true)
	Iterable<OAuthClientSession> findUserSessions(@Param("userAccountId") int userAccountId);
	
	/** Find all sessions associated with a user account. */
	Iterable<OAuthClientSession> findOAuthClientSessionByUserAccount(UserAccount userAccount);
	
}
