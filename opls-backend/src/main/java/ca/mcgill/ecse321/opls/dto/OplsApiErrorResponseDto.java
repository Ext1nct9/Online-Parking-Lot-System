package ca.mcgill.ecse321.opls.dto;

import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for an API error.
 */
public class OplsApiErrorResponseDto {

	/**
	 * Brief error code.
	 */
	@JsonProperty("error")
	public String error;

	/**
	 * Message explaining the error.
	 */
	@JsonProperty("error_description")
	public String message;

	/**
	 * Custom fields to return in the error response.
	 */
	@JsonProperty("fields")
	public Hashtable<String, String> fields = new Hashtable<String, String>();

	/** Create an error response. */
	public OplsApiErrorResponseDto(String error, String message) {
		this.error = error;
		this.message = message;
	}

	/** Create an error response. */
	public OplsApiErrorResponseDto(String error) {
		this(error, null);
	}

	/** Create an error response. */
	public OplsApiErrorResponseDto() {
		this(null, null);
	}

	/** Add a field to the field list of the response. */
	public void addField(String field, String value) {
		this.fields.put(field, value);
	}

	/**
	 * Determine if a field exists.
	 * 
	 * @param field
	 *            The field to test.
	 * @param value
	 *            The target value, null if do not compare the value.
	 * @return If the field and value match.
	 */
	public boolean hasField(String field, String value) {
		return fields.containsKey(field)
				&& (value == null || fields.get(field).equals(value));
	}

}
