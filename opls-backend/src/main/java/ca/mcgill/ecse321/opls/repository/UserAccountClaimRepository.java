package ca.mcgill.ecse321.opls.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.model.auth.UserAccountClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccountClaim.UserAccountClaimId;

/**
 * Repository to interface with the user_account_claim table.
 */
public interface UserAccountClaimRepository extends CrudRepository<UserAccountClaim, UserAccountClaimId> {
	
	/** Get all claims for a user account. */
	@Query(value = "SELECT claim FROM user_account_claim WHERE user_account_id = :userAccountId", nativeQuery = true)
	Iterable<String> getUserClaims(@Param("userAccountId") int userAccountId);
	
	/** Get all claims for a user account. */
	default Iterable<OAuthClaim> getUserClaims(UserAccount userAccount) {
		var claims = getUserClaims(userAccount.getId());
		if (claims == null) {
			return null;
		}
		
		var ret = new ArrayList<OAuthClaim>();
		claims.forEach((claim) -> {
			ret.add(OAuthClaim.fromString(claim));
		});
		return ret;
	}
	
	/** Determine if a user has a specific claim. */
	@Query(value = "SELECT EXISTS(SELECT 1 FROM user_account_claim WHERE user_account_id = :userAccountId AND claim = :claim)", nativeQuery = true)
	boolean userHasClaim(@Param("userAccountId") int userAccountId, @Param("claim") String claim);

	/** Determine if a user has a specific claim. */
	default boolean userHasClaim(UserAccount userAccount, OAuthClaim claim) {
		return userHasClaim(userAccount.getId(), claim.toString());
	}
	
}
