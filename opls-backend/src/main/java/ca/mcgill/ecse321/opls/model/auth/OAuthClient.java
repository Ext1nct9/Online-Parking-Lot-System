package ca.mcgill.ecse321.opls.model.auth;

import ca.mcgill.ecse321.opls.model.UuidModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.*;

/**
 * Model for a connecting client.
 */
@Entity
public class OAuthClient extends UuidModel {
	@Column(unique = true, nullable = false, length = OAUTH_CLIENT_CLIENTID_LEN)
	private String clientId;

	@Column(nullable = false, length = OAUTH_CLIENT_SECRET_LEN)
	private String secret;

	@Column(unique = true, nullable = false, length = OAUTH_CLIENT_NAME_MAXLEN)
	private String name;
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public void setRandomClientId() {
		this.clientId = randomClientId();
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public void setRandomSecret() {
		this.secret = randomClientSecret();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.length() > OAUTH_CLIENT_NAME_MAXLEN ? name.substring(0, OAUTH_CLIENT_NAME_MAXLEN) : name;
	}
	
	/** Create a new session with a random refresh token. */
	public OAuthClientSession newSession() {
		var session = new OAuthClientSession();
		session.setClient(this);
		session.setRandomRefreshToken();
		return session;
	}
	
	/** Default constructor. */
	public OAuthClient() {
		super();
	}
	
	/** Create an OAuthClient with name, ID, and secret fields. */
	public OAuthClient(String name, String clientId,
			String secret) {
		super();
		this.name = name;
		this.clientId = clientId;
		this.secret = secret;
	}
	
}
