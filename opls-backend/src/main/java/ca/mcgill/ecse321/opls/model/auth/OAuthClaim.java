package ca.mcgill.ecse321.opls.model.auth;

/**
 * Enumeration of all possible claim values.
 */
public enum OAuthClaim {
	/** None, default. */
	NONE,

	/** Administrator. */
	ADMIN,

	/** Employee. */
	EMPLOYEE,

	/** Customer. */
	CUSTOMER;
	
	public static OAuthClaim fromString(String claim) {
		claim = claim.toUpperCase();
		for (var claimEnum : OAuthClaim.values()) {
			if (claimEnum.toString().equals(claim)) {
				return claimEnum;
			}
		}
		
		return OAuthClaim.NONE;
	}
}
