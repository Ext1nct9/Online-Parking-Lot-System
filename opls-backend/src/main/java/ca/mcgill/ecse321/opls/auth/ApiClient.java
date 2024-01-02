package ca.mcgill.ecse321.opls.auth;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.tokenRequestHeaders;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import ca.mcgill.ecse321.opls.dto.auth.TokenRequestDto;
import ca.mcgill.ecse321.opls.dto.auth.TokenResponseDto;
import ca.mcgill.ecse321.opls.exception.OplsApiException;

public class ApiClient {

	private String rootUrl;

	private RestTemplate apiClient;
	private String clientId;
	private String clientSecret;

	private TokenResponseDto credentials;

	/**
	 * Create an API client.
	 * 
	 * @param rootUrl
	 *            The root URL to request from.
	 * @param builder
	 *            The builder for the api client.
	 * @param clientId
	 *            The registered client ID for the OAuthClient.
	 * @param clientSecret
	 *            The corresponding secret.
	 */
	public ApiClient(String rootUrl, RestTemplateBuilder builder,
			String clientId, String clientSecret) {
		this.rootUrl = rootUrl;
		this.apiClient = builder
				.additionalMessageConverters(
						new MappingJackson2HttpMessageConverter())
				.additionalMessageConverters(new FormHttpMessageConverter())
				.setConnectTimeout(Duration.ofSeconds(10))
				.setReadTimeout(Duration.ofSeconds(10))
				.requestFactory(HttpComponentsClientHttpRequestFactory.class)
				.errorHandler(new DefaultResponseErrorHandler() {
					@Override
					public boolean hasError(ClientHttpResponse response)
							throws IOException {
						HttpStatusCode statusCode = response.getStatusCode();
						return statusCode.is5xxServerError();
					}
				}).build();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	/**
	 * Populate HTTP header fields.
	 *
	 * @param headers
	 *            The headers to build upon, null if create a new one.
	 * @param addBearer
	 *            Whether to add bearer authentication.
	 * @param hasBody
	 *            Whether the request has a body.
	 * @return The populated headers.
	 */
	private HttpHeaders defaultHeaders(HttpHeaders headers, boolean addBearer,
			boolean hasBody) {
		if (headers == null) {
			headers = new HttpHeaders();
		}

		// set authorization
		if (addBearer && credentials != null) {
			headers.setBearerAuth(credentials.accessToken);
		}

		// set content types
		if (hasBody && headers.getContentType() == null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
		}
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		return headers;
	}

	/**
	 * Send an HTTP request with a request body.
	 * 
	 * @param <T>
	 *            The request body type.
	 * @param <U>
	 *            The expected response type.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The target endpoint.
	 * @param body
	 *            The request body.
	 * @param headers
	 *            The HTTP headers to send.
	 * @param responseClass
	 *            The expected response type.
	 * @return The response entity.
	 */
	public <T, U> ResponseEntity<U> exchange(HttpMethod method, String endpoint,
			T body, HttpHeaders headers, Class<U> responseClass) {
		var entity = new HttpEntity<T>(body, headers);
		var resp = apiClient.exchange(rootUrl + endpoint, method, entity,
				responseClass);
		return resp;
	}

	/**
	 * Send an HTTP request without a request body.
	 * 
	 * @param <U>
	 *            The expected response type.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The target endpoint.
	 * @param headers
	 *            The HTTP headers to send.
	 * @param responseClass
	 *            The expected response type.
	 * @return The response entity.
	 */
	public <U> ResponseEntity<U> exchange(HttpMethod method, String endpoint,
			HttpHeaders headers, Class<U> responseClass) {
		var entity = new HttpEntity<Object>(null, headers);
		var resp = apiClient.exchange(rootUrl + endpoint, method, entity,
				responseClass);
		return resp;
	}
	
	/**
	 * Make a refresh token request.
	 */
	public void refreshAuthentication() {
		authenticate(TokenRequestDto.refreshTokenRequest(this.credentials.refreshToken));
	}

	/**
	 * Start an authorization flow for the client.
	 * 
	 * @param tokenRequest
	 *            The authorization request.
	 */
	public void authenticate(TokenRequestDto tokenRequest) {
		var reqMap = tokenRequest.toRequestMap();
		var resp = this.exchange(HttpMethod.POST, "/token", reqMap,
				tokenRequestHeaders(clientId, clientSecret),
				TokenResponseDto.class);

		if (resp != null && resp.getStatusCode() == HttpStatus.OK) {
			credentials = resp.getBody();
			return;
		}

		throw new OplsApiException(resp.getStatusCode(),
				"Failed authentication.");
	}

	/**
	 * @return The client's current credentials;
	 */
	public TokenResponseDto getCredentials() {
		return credentials;
	}

	/**
	 * Refresh the client's credentials if needed.
	 */
	public void updateCredentials() {
		if (credentials == null) {
			throw new OplsApiException(HttpStatus.UNAUTHORIZED,
					"Must provide initial authentication through authenticate().");
		}

		if (credentials.expiresOn < new Date().getTime()
				+ TimeUnit.MINUTES.toMillis(1)) {
			// get refresh token
			var refreshReq = TokenRequestDto
					.refreshTokenRequest(credentials.refreshToken);
			authenticate(refreshReq);
		}
	}

	/**
	 * Send an HTTP request with a request body.
	 * 
	 * @param <T>
	 *            The request body type.
	 * @param <U>
	 *            The expected response type.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The target endpoint.
	 * @param body
	 *            The request body.
	 * @param responseClass
	 *            The expected response type.
	 * @return The response entity.
	 */
	public <T, U> ResponseEntity<U> exchange(HttpMethod method, String endpoint,
			T body, Class<U> responseClass) {
		updateCredentials();

		return exchange(method, endpoint, body,
				defaultHeaders(null, true, true), responseClass);
	}

	/**
	 * Send an HTTP request without a request body.
	 * 
	 * @param <U>
	 *            The expected response type.
	 * @param method
	 *            The request method.
	 * @param endpoint
	 *            The target endpoint.
	 * @param responseClass
	 *            The expected response type.
	 * @return The response entity.
	 */
	public <U> ResponseEntity<U> exchange(HttpMethod method, String endpoint,
			Class<U> responseClass) {
		updateCredentials();

		return exchange(method, endpoint, defaultHeaders(null, true, false),
				responseClass);
	}

}
