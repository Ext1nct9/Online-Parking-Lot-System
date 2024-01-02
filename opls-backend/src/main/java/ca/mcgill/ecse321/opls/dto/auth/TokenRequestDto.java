package ca.mcgill.ecse321.opls.dto.auth;

import java.beans.ConstructorProperties;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import ca.mcgill.ecse321.opls.model.auth.OAuthGrantType;

/**
 * Request for token credentials.
 */
public class TokenRequestDto {

	/** Constructor to initialize the request from form parameters. */
	@ConstructorProperties({"grant_type", "username", "password",
			"refresh_token"})
	public TokenRequestDto(String grantType, String username, String password,
			String refreshToken) {
		this.grantType = OAuthGrantType.fromString(grantType);
		this.username = username;
		this.password = password;
		this.refreshToken = refreshToken;
	}

	/**
	 * The requested grant.
	 */
	public OAuthGrantType grantType;

	/**
	 * The username to log in with. Required for password grants.
	 */
	public String username;

	/**
	 * The password to log in with. Required for password grants.
	 */
	public String password;

	/**
	 * The refresh token to identify the session. Required for refresh_token
	 * grants.
	 */
	public String refreshToken;

	/** Create a client credentials request. */
	public static TokenRequestDto clientCredentialsRequest() {
		return new TokenRequestDto(OAuthGrantType.CLIENT_CREDENTIALS.toString(),
				null, null, null);
	}

	/** Create a password request. */
	public static TokenRequestDto passwordRequest(String username,
			String password) {
		return new TokenRequestDto(OAuthGrantType.PASSWORD.toString(), username,
				password, null);
	}

	/** Create a refresh token request. */
	public static TokenRequestDto refreshTokenRequest(String refreshToken) {
		return new TokenRequestDto(OAuthGrantType.REFRESH_TOKEN.toString(),
				null, null, refreshToken);
	}

	/**
	 * Convert the token request to form data.
	 */
	public MultiValueMap<String, String> toRequestMap() {
		var ret = new LinkedMultiValueMap<String, String>();

		ret.add("grant_type", grantType.toString());

		if (grantType == OAuthGrantType.PASSWORD) {
			ret.add("username", username);
			ret.add("password", password);
		} else if (grantType == OAuthGrantType.REFRESH_TOKEN) {
			ret.add("refresh_token", refreshToken);
		}

		return ret;
	}

	/**
	 * Convert the token request to stringified form data.
	 */
	@Override
	public String toString() {
		String req = "grant_type=" + grantType.toString();

		switch (grantType) {
			case PASSWORD :
				req += "&username=" + username + "&password=" + password;
				break;
			case REFRESH_TOKEN :
				req += "&refresh_token=" + refreshToken;
				break;
			default :
				break;
		}

		return req;
	}

}
