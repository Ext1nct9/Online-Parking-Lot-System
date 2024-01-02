package ca.mcgill.ecse321.opls.service.auth;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.OAUTH_ACCESS_TOKEN_EXPIRY;
import static ca.mcgill.ecse321.opls.auth.OAuthHelper.OAUTH_REFRESH_TOKEN_EXPIRY;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.auth.OAuthHelper;
import ca.mcgill.ecse321.opls.dto.auth.AccessToken;
import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;
import ca.mcgill.ecse321.opls.model.auth.OAuthGrantType;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

/**
 * Service to fulfill token grant requests.
 */
@Service
public class OAuthGrantService {

	@Autowired
	private UserAccountAuthService userService;

	@Autowired
	private OAuthSessionService sessionService;

	/**
	 * Fulfill a token request.
	 * 
	 * @param requestingClient
	 *            The OAuthClient requesting the access token.
	 * @param request
	 *            The request body.
	 * @param token
	 *            The token object to populate.
	 * @return The stored session, if the grant is refreshable.
	 */
	public OAuthClientSession populateAccessToken(OAuthClient requestingClient,
			TokenRequestDto request, AccessToken token) {
		token.oauthClientId = requestingClient.getClientId();

		// validate the grant
		UserAccount user = null;
		if (request.grantType == OAuthGrantType.PASSWORD) {
			if (request.username == null || request.password == null) {
				throw OAuthHelper.Errors
						.invalidGrant("Missing username or password.");
			}

			user = userService.authenticateUser(request.username,
					request.password);
		} else if (request.grantType == OAuthGrantType.CLIENT_CREDENTIALS) {
			token.isRegistered = false;
			token.oauthClaims.add(OAuthClaim.CUSTOMER);
		} else if (request.grantType == OAuthGrantType.REFRESH_TOKEN) {
			if (request.refreshToken == null) {
				throw OAuthHelper.Errors.invalidGrant("Missing refresh token.");
			}

			// validate refresh token
			var session = sessionService.validateRefreshToken(
					request.refreshToken, requestingClient);
			user = session.getUserAccount();

			// delete session
			sessionService.deleteSession(session);
		}

		// populate user account values
		if (user != null) {
			token.userAccountId = user.getId();
			token.oauthClaims.addAll(userService.getUserClaims(user));
			token.isRegistered = true;
		}

		// set expiry
		long now = new Date().getTime();
		token.expiresOn = now + OAUTH_ACCESS_TOKEN_EXPIRY;

		// generate refreshable session
		if (request.grantType.isRefreshable()) {
			return sessionService.newSession(requestingClient, user,
					now + OAUTH_REFRESH_TOKEN_EXPIRY);
		} else {
			return null;
		}
	}

}
