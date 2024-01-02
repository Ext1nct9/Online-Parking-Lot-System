package ca.mcgill.ecse321.opls.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Model for a request to update user information.
 */
public class UpdateUserAccountRequestDto {

	/**
	 * User's username.
	 */
	@NotNull
	@Size(min = 6, message = "The field must be at least 6 characters!")
	public String username;

	/**
	 * First name.
	 */
	@NotNull
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String firstName;

	/**
	 * Last name.
	 */
	@NotNull
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String lastName;

	/**
	 * Default constructor
	 */
	public UpdateUserAccountRequestDto() {
	}

	/**
	 * Constructor with fields
	 */
	public UpdateUserAccountRequestDto(String username, String firstName,
			String lastName) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
	}

}
