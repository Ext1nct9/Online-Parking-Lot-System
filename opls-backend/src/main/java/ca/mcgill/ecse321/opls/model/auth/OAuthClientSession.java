package ca.mcgill.ecse321.opls.model.auth;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OAuthClientSession {
	
	@Id
	@Column(nullable = false, length = OAUTH_REFRESH_TOKEN_LEN)
	private String refreshToken;
	
	private long expiry;
	
	/** A session must have a client. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "oauth_client_id", nullable = false)
	private OAuthClient client;
	
	/** A session may not have an authorized user. */
	@ManyToOne(optional = true)
	@JoinColumn(name = "user_account_id", nullable = true)
	private UserAccount userAccount;
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public void setRandomRefreshToken() {
		this.refreshToken = randomRefreshToken();
	}

	public long getExpiry() {
		return expiry;
	}

	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

	public OAuthClient getClient() {
		return client;
	}

	public void setClient(OAuthClient client) {
		this.client = client;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
	
}
