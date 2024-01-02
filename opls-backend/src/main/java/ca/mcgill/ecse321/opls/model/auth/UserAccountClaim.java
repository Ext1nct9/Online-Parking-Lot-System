package ca.mcgill.ecse321.opls.model.auth;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Model to link a user type to the user account.
 */
@Entity
@Table(name = "user_account_claim")
public class UserAccountClaim {
	@EmbeddedId
	UserAccountClaimId id;
	
	/** Default constructor. */
	public UserAccountClaim() {}
	
	/** Constructor linking the type to a user. */
	public UserAccountClaim(UserAccount userAccount, OAuthClaim oAuthClaim) {
		this.id = new UserAccountClaimId(userAccount, oAuthClaim);
	}
	
	public static class UserAccountClaimId implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Enumerated(EnumType.STRING)
		@Column(nullable = false, length = 15)
		private OAuthClaim claim = OAuthClaim.NONE;
		
		@ManyToOne(optional = false)
		@JoinColumn(name = "user_account_id", nullable = false)
		private UserAccount userAccount;
		
		/** Default constructor. */
		public UserAccountClaimId() {}
		
		/** Constructor with parameters. */
		public UserAccountClaimId(UserAccount userAccount, OAuthClaim oAuthClaim) {
			this.claim = oAuthClaim;
			this.userAccount = userAccount;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof UserAccountClaimId)) {
				return false;
			}
			
			UserAccountClaimId id = (UserAccountClaimId)o;
			
			return this.userAccount.getId() == id.userAccount.getId() && this.claim == id.claim;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.userAccount, this.claim);
		}
	}

	/** Get the claim value. */
	public OAuthClaim getClaim() {
		return this.id.claim;
	}

	/** Get the associated user. */
	public UserAccount getUserAccount() {
		return this.id.userAccount;
	}
}