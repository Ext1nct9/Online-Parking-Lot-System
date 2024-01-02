package ca.mcgill.ecse321.opls.dto.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

/**
 * User account response model.
 */
public class UserAccountResponseDto {

	/**
	 * UUID of user account.
	 */
	public UUID uuid;

	/**
	 * Username of account.
	 */
	public String username;

	/**
	 * First name of user.
	 */
	public String firstname;

	/**
	 * Last name of user.
	 */
	public String lastname;

	/**
	 * Security question for user.
	 */
	public String securityQuestion;

	/**
	 * List of claims associated to user account
	 */
	public List<OAuthClaim> claims;
	
	/** Default constructor. */
	public UserAccountResponseDto() {
	}

	/** Constructor for user account from an existing user account. */
	public UserAccountResponseDto(UserAccount ua) {
		this.username = ua.getUsername();
		this.firstname = ua.getFirstName();
		this.lastname = ua.getLastName();
		this.securityQuestion = ua.getSecurityQuestion();
		this.uuid = ua.getUuid();
		var claims = ua.getClaims();
		if (claims != null) {
			this.claims = new ArrayList<OAuthClaim>();
			for (var claim : claims) {
				this.claims.add(claim.getClaim());
			}
		}
	}

}
