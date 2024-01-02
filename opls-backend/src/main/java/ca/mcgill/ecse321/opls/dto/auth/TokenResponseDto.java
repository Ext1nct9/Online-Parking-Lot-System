package ca.mcgill.ecse321.opls.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token grant response with credentials.
 */
public class TokenResponseDto {

	/**
	 * The access token to pass in with each request.
	 */
	@JsonProperty("access_token")
	public String accessToken;

	/**
	 * The type of token (defaults to "bearer").
	 */
	@JsonProperty("token_type")
	public String tokenType;

	/**
	 * The token used to refresh the session if applicable.
	 */
	@JsonProperty("refresh_token")
	public String refreshToken;

	/**
	 * How long the access token is valid for.
	 */
	@JsonProperty("expires_in")
	public long expiresIn;

	/**
	 * Absolute time when the token expires.
	 */
	@JsonProperty("expires_on")
	public long expiresOn;

}
