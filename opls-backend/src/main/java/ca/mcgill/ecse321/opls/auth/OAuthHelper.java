package ca.mcgill.ecse321.opls.auth;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import ca.mcgill.ecse321.opls.exception.OplsApiException;

/**
 * Static helper methods for the OAuth2 flow.
 */
public class OAuthHelper {

	/** Access tokens expire in 60 minutes. */
	public static final long OAUTH_ACCESS_TOKEN_EXPIRY = 60 * 60 * 1000;

	/** Refresh tokens expire in 24 hours. */
	public static final long OAUTH_REFRESH_TOKEN_EXPIRY = 24 * 60 * 60 * 1000;

	public static final int OAUTH_CLIENT_CLIENTID_LEN = 16;
	public static final int OAUTH_CLIENT_SECRET_LEN = 32;
	public static final int OAUTH_CLIENT_NAME_MAXLEN = 32;
	public static final int OAUTH_REFRESH_TOKEN_LEN = 32;

	private static final String RND_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "abcdefghijklmnopqrstuvwxyz" + "0123456789";

	/**
	 * Generate a random string.
	 */
	public static String randomString(int length) {
		Random rnd = new Random();

		// get random bytes
		char[] randChars = new char[length];
		for (int i = 0; i < length; ++i) {
			randChars[i] = RND_CHARS
					.charAt(Math.abs(rnd.nextInt()) % RND_CHARS.length());
		}

		return String.copyValueOf(randChars);
	}

	/** Generate a random client secret. */
	public static String randomClientSecret() {
		return randomString(OAUTH_CLIENT_SECRET_LEN);
	}

	/** Generate a random client ID. */
	public static String randomClientId() {
		return randomString(OAUTH_CLIENT_CLIENTID_LEN);
	}

	/** Generate a random refresh token. */
	public static String randomRefreshToken() {
		return randomString(OAUTH_REFRESH_TOKEN_LEN);
	}

	/**
	 * Generate a headers object for a request to a token endpoint.
	 * 
	 * @param clientId
	 *            The ID of the client.
	 * @param clientSecret
	 *            The secret of the client.
	 * @return The headers with the authorization and content type.
	 */
	public static HttpHeaders tokenRequestHeaders(String clientId,
			String clientSecret) {
		var clientCreds = generateBasicCredentials(clientId, clientSecret);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBasicAuth(clientCreds);
		return headers;
	}

	/**
	 * Generate encoded credentials for the client.
	 * 
	 * @param clientId
	 *            The client ID.
	 * @param clientSecret
	 *            The client secret.
	 * @return The encoded credentials.
	 */
	public static String generateBasicCredentials(String clientId,
			String clientSecret) {
		var clientIdSecret = (clientId + ":" + clientSecret);
		return Base64.getUrlEncoder().encodeToString(clientIdSecret.getBytes());
	}

	/**
	 * Generate an authorization header to pass to the token endpoint.
	 * 
	 * @param clientId
	 *            The client ID.
	 * @param clientSecret
	 *            The client secret.
	 * @return The concatenated and encoded header.
	 */
	public static String generateBasicHeader(String clientId,
			String clientSecret) {
		return "basic " + generateBasicCredentials(clientId, clientSecret);
	}

	/** Helper methods to generate OAuth errors. */
	public static class Errors {
		public static OplsApiException invalidRequest(String description) {
			return new OplsApiException(HttpStatus.BAD_REQUEST,
					"invalid_request", description);
		}

		public static OplsApiException invalidClient(String description) {
			return new OplsApiException(HttpStatus.BAD_REQUEST,
					"invalid_client", description);
		}

		public static OplsApiException invalidGrant(String description) {
			return new OplsApiException(HttpStatus.UNAUTHORIZED,
					"invalid_grant", description);
		}

		public static OplsApiException unauthorizedClient(String description) {
			return new OplsApiException(HttpStatus.UNAUTHORIZED,
					"unauthorized_client", description);
		}

		public static OplsApiException unsupportedGrantType() {
			return new OplsApiException(HttpStatus.BAD_REQUEST,
					"unsupported_grant_type");
		}

		public static OplsApiException unauthorized(String description) {
			return new OplsApiException(HttpStatus.UNAUTHORIZED, "unauthorized",
					description);
		}
	}

}
