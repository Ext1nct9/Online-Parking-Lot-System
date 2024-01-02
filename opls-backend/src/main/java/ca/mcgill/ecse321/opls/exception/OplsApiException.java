package ca.mcgill.ecse321.opls.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception model to be thrown and caught by the API.
 */
public class OplsApiException extends ResponseStatusException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create an exception object.
	 * 
	 * @param status
	 *            The HTTP status.
	 * @param error
	 *            The brief error title.
	 * @param description
	 *            The description of the error.
	 */
	public OplsApiException(HttpStatusCode status, String error,
			String description) {
		super(status, error, null, description, null);
	}

	/**
	 * Create an exception object.
	 * 
	 * @param status
	 *            The HTTP status.
	 * @param error
	 *            The brief error title.
	 */
	public OplsApiException(HttpStatusCode status, String error) {
		this(status, error, null);
	}

}
