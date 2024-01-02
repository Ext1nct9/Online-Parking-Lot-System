package ca.mcgill.ecse321.opls.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.dto.auth.AccessToken;
import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.dto.auth.TokenResponseDto;
import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.service.auth.OAuthClientService;
import ca.mcgill.ecse321.opls.service.auth.OAuthGrantService;

/**
 * Controller for OAuth2 operations.
 */
@RestController
@CrossOrigin
public class AuthController {

	@Autowired
	private OAuthClientService clientService;

	@Autowired
	private OAuthGrantService grantService;

	/**
	 * OAuth2 endpoint to grant access tokens.
	 * 
	 * @HTTPMethod			POST
	 * @URL					/token
	 * @param authorization	The basic authorization header for the requesting OAuthClient.
	 * @param tokenRequest 	The token request body.
	 * @return 				The granted credentials.
	 */
	@PostMapping(value = "/token", consumes = {
			MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {
					MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<TokenResponseDto> tokenEndpoint(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			TokenRequestDto tokenRequest) {

		/** Populate access token. */
		var token = new AccessToken();

		// get the requesting client
		var client = clientService
				.getOAuthClientFromAuthorizationHeader(authorization);

		// validate the grant request
		var session = grantService.populateAccessToken(client, tokenRequest,
				token);

		/** Generate response. */
		try {
			TokenResponseDto res = AccessTokenHelper
					.generateTokenResponse(token, session);

			// generate headers to invalidate caching
			HttpHeaders headers = new HttpHeaders();
			headers.setCacheControl(CacheControl.noStore());
			headers.setPragma(CacheControl.noCache().getHeaderValue());

			return new ResponseEntity<TokenResponseDto>(res, headers,
					HttpStatus.OK);
		} catch (JsonProcessingException e) {
			throw new OplsApiException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Could not generate token");
		}
	}

}
