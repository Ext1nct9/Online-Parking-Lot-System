package ca.mcgill.ecse321.opls.model.auth;

import ca.mcgill.ecse321.opls.auth.OAuthHelper;

public enum OAuthGrantType {

	/** Password. */
	PASSWORD("password", true),

	/** Client credentials. */
	CLIENT_CREDENTIALS("client_credentials", false),

	/** Refresh token. */
	REFRESH_TOKEN("refresh_token", true);

	private String grantType;

	private boolean refreshable;

	OAuthGrantType(String grantType, boolean refreshable) {
		this.grantType = grantType;
		this.refreshable = refreshable;
	}

	/**
	 * Get the grant type ID.
	 */
	@Override
	public String toString() {
		return this.grantType;
	}

	/**
	 * Whether the grant type is refreshable.
	 */
	public boolean isRefreshable() {
		return refreshable;
	}

	/** Parse the grant type enum from the string. */
	public static OAuthGrantType fromString(String grantType) {
		for (var gt : OAuthGrantType.values()) {
			if (gt.toString().equals(grantType)) {
				return gt;
			}
		}

		throw OAuthHelper.Errors.unsupportedGrantType();
	}

}
