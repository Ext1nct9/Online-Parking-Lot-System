package ca.mcgill.ecse321.opls.dto.auth;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;

/**
 * Access token credentials.
 */
public class AccessToken {

	/**
	 * ID of the client for the session.
	 */
	public String oauthClientId;

	/**
	 * Whether the session belongs to a registered user.
	 */
	public boolean isRegistered;

	/**
	 * The integer ID for the registered user's account.
	 */
	public int userAccountId;

	/**
	 * The list of OAuthClaim values for the session.
	 */
	public Set<OAuthClaim> oauthClaims;

	/**
	 * When the token expires.
	 */
	public long expiresOn;

	/** Default constructor. Initializes the claim set as empty. */
	public AccessToken() {
		oauthClaims = new HashSet<OAuthClaim>();
	}

	/** Determine whether the token has a specific claim. */
	public boolean hasClaim(OAuthClaim claim) {
		return oauthClaims.contains(claim);
	}

	/** Get the user ID if the token has the customer claim, null otherwise. */
	@JsonIgnore
	public Integer getCustomerUserId() {
		return isRegistered && hasClaim(OAuthClaim.CUSTOMER)
				? userAccountId
				: null;
	}

	/** Get the user ID of the token has the employee claim, null otherwise. */
	@JsonIgnore
	public Integer getEmployeeUserId() {
		return isRegistered && hasClaim(OAuthClaim.EMPLOYEE)
				? userAccountId
				: null;
	}

	/** Determine whether the token is expired. */
	@JsonIgnore
	public boolean isExpired() {
		return expiresOn < new Date().getTime();
	}

}
