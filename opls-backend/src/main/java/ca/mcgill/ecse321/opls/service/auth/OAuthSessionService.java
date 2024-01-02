package ca.mcgill.ecse321.opls.service.auth;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.auth.OAuthHelper;
import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.OAuthClientSessionRepository;
import jakarta.transaction.Transactional;

/**
 * Service to handle creating and validating refreshable sessions.
 */
@Service
public class OAuthSessionService {

	@Autowired
	private OAuthClientSessionRepository sessionRepo;
	
	/**
	 * Validate the refresh token in the request.
	 * @param refreshToken The refresh token.
	 * @param requestingClient The client making the request.
	 * @return The existing session entry.
	 * @throws OplsApiException if the refresh token is invalid.
	 */
	@Transactional
	public OAuthClientSession validateRefreshToken(String refreshToken, OAuthClient requestingClient) {
		var session = sessionRepo.findSessionByRefreshToken(refreshToken);
		
		// ensure session exists
		if (session == null) {
			throw OAuthHelper.Errors.invalidGrant("Invalid refresh token.");
		}
		
		// ensure session belongs to the client
		if (!requestingClient.getClientId().equals(session.getClient().getClientId())) {
			throw OAuthHelper.Errors.invalidGrant("Invalid refresh token.");
		}
		
		// ensure session is not expired
		if (session.getExpiry() < new Date().getTime()) {
			// delete session
			deleteSession(session);
			
			throw OAuthHelper.Errors.invalidGrant("Expired refresh token.");
		}
		
		return session;
	}
	
	/**
	 * Delete an existing session.
	 * @param session The client session to delete.
	 */
	@Transactional
	public void deleteSession(OAuthClientSession session) {
		sessionRepo.delete(session);
	}
	
	/**
	 * Create a new refreshable session.
	 * @param client The client for the session (required).
	 * @param user The user for the session (optional).
	 * @param expiry The expiry date of the session.
	 * @return The created session object.
	 */
	@Transactional
	public OAuthClientSession newSession(OAuthClient client, UserAccount user, long expiry) {
		var session = new OAuthClientSession();
		session.setRandomRefreshToken();
		session.setExpiry(expiry);
		session.setClient(client);
		session.setUserAccount(user);
		sessionRepo.save(session);
		
		return session;
	}
	
}
