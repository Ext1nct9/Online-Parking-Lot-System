package ca.mcgill.ecse321.opls.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Response model for a security question.
 */
public class SecurityQuestionResponseDto {

	/**
	 * Security question.
	 */
	@NotNull
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String securityQuestion;

	/**
	 * Default constructor
	 */
	public SecurityQuestionResponseDto() {
	}

	/**
	 * Constructor with fields
	 */
	public SecurityQuestionResponseDto(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}
}
