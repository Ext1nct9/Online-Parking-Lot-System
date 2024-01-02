package ca.mcgill.ecse321.opls.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Model for a reset security question and answer request.
 */
public class ResetSecurityQuestionAnswerRequestDto {

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
	public ResetSecurityQuestionAnswerRequestDto() {
	}

	/** Constructor with fields. */
	public ResetSecurityQuestionAnswerRequestDto(String pass, String newQ,
			String newA) {
		this.password = pass;
		this.securityQuestion = newQ;
		this.securityAnswer = newA;
	}

}
