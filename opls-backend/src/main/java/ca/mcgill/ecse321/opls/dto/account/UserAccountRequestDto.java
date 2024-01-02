package ca.mcgill.ecse321.opls.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * User account request model.
 */
public class UserAccountRequestDto {

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
	 * Password.
	 */
	@NotNull
	@Size(min = 8, message = "The field must be at least 8 characters!")
	public String password;

	/**
	 * Security question.
	 */
	@NotNull
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String securityQuestion;

	/**
	 * Security answer.
	 */
	@NotNull
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String securityAnswer;

	/** Default constructor. */
	public UserAccountRequestDto() {
	}

	/** Constructor with fields. */
	public UserAccountRequestDto(String username, String firstName,
			String lastName, String password, String securityQuestion,
			String securityQuestionAnswer) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.securityQuestion = securityQuestion;
		this.securityAnswer = securityQuestionAnswer;
	}

}
