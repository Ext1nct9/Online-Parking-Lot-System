package ca.mcgill.ecse321.opls.auth;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.OAUTH_ACCESS_TOKEN_EXPIRY;

import java.util.Base64;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.dto.auth.AccessToken;
import ca.mcgill.ecse321.opls.dto.auth.TokenResponseDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.OAuthClientSession;

/**
 * Static helper methods for the access token.
 */
public class AccessTokenHelper {

	private static byte[] TOKEN_HASH_SALT_BYTES = "abcdefghi".getBytes();

	private static String TOKEN_HASH_SALT = Base64.getUrlEncoder()
			.encodeToString(TOKEN_HASH_SALT_BYTES);

	private static String encryptAccessToken(AccessToken token)
			throws JsonProcessingException {
		// serialize to JSON
		String str = new ObjectMapper().writeValueAsString(token);

		// "encrypt" using base64
		return Base64.getUrlEncoder().encodeToString(str.getBytes());
	}

	private static AccessToken decryptAccessToken(String encrypted)
			throws JsonProcessingException {
		// "decrypt" using base64
		String str = new String(Base64.getUrlDecoder().decode(encrypted));

		// deserialize from JSON
		return new ObjectMapper().readerFor(AccessToken.class).readValue(str);
	}

	/**
	 * Generate a token endpoint response.
	 * 
	 * @param token
	 *            The access token.
	 * @param session
	 *            The refreshable session, if exists.
	 * @return The object with required fields.
	 * @throws JsonProcessingException
	 *             If the object mapper fails.
	 */
	public static TokenResponseDto generateTokenResponse(AccessToken token,
			OAuthClientSession session) throws JsonProcessingException {
		var encryptedToken = encryptAccessToken(token);
		var tokenHash = HashHelper.hashData(encryptedToken,
				TOKEN_HASH_SALT_BYTES);

		var res = new TokenResponseDto();
		res.tokenType = "bearer";
		res.accessToken = encryptedToken + "." + tokenHash.getHashedData();
		res.expiresIn = OAUTH_ACCESS_TOKEN_EXPIRY;
		res.expiresOn = token.expiresOn;

		if (session != null) {
			res.refreshToken = session.getRefreshToken();
		}

		return res;
	}

	/**
	 * Parse the access token and validate it.
	 * @param authorizationHeader The authorization string.
	 * @param mustBeRegistered Whether the user must be registered for the endpoint.
	 * @param requiredClaims List of possible claims a user must have to access this endpoint.
	 * @return The parsed access token.
	 */
	public static AccessToken parseAccessToken(String authorizationHeader,
			boolean mustBeRegistered, Collection<OAuthClaim> requiredClaims) {
		/** Determine if provide default token. */
		String provideDefaultStr = System.getenv("opls_default_auth");
		if (provideDefaultStr != null && Boolean.parseBoolean(provideDefaultStr)) {
			String isTestModeStr = System.getenv("opls_test_mode");
			boolean isTestMode = isTestModeStr != null && Boolean.parseBoolean(isTestModeStr);
			
			// do not default if in test mode
			if (!isTestMode) {
				return OplsStartupService.createDefaultAccessToken();
			}
		}
		
		if (authorizationHeader == null) {
			throw OAuthHelper.Errors
				.invalidRequest("Missing Authorization header.");
		}
		
		/** Parse token. */
		AccessToken token = null;
		try {
			// decode the header
			var tokens = authorizationHeader.split(" ");
			if (!tokens[0].equalsIgnoreCase("bearer")) {
				throw new Exception();
			}
			
			// test token hash
			tokens = tokens[1].split("[.]");
			if (!HashHelper.testHash(tokens[0], TOKEN_HASH_SALT, tokens[1])) {
				throw new Exception();
			}

			token = decryptAccessToken(tokens[0]);
		} catch (Exception e) {
			throw OAuthHelper.Errors
					.invalidRequest("Malformed Authorization header.");
		}

		/** Validate request. */
		if (mustBeRegistered && !token.isRegistered) {
			throw OAuthHelper.Errors.unauthorized("User must be registered.");
		}
		if (token.isExpired()) {
			throw OAuthHelper.Errors.unauthorized("Expired access token.");
		}

		/** Validate claims. */
		if (requiredClaims != null && requiredClaims.size() > 0) {
			boolean matchedClaim = false;
			for (var possibleClaim : requiredClaims) {
				if (token.hasClaim(possibleClaim)) {
					matchedClaim = true;
					break;
				}
			}
			if (!matchedClaim) {
				throw OAuthHelper.Errors.unauthorized("Invalid claims.");
			}
		}

		return token;
	}

}
