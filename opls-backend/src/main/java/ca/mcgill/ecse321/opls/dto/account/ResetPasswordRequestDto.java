package ca.mcgill.ecse321.opls.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request model to reset password.
 */
public class ResetPasswordRequestDto {

	/**
	 * Password.
	 */
	@NotNull
	@Size(min = 8, message = "The field must be at least 8 characters!")
	public String password;

	/**
	 * Security answer.
	 */
	@NotNull
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String securityAnswer;

	/** Default constructor. */
	public ResetPasswordRequestDto() {
	}
	
	/** Constructor with fields. */
	public ResetPasswordRequestDto(String newpass, String oldA) {
		this.password = newpass;
		this.securityAnswer = oldA;
	}

}
