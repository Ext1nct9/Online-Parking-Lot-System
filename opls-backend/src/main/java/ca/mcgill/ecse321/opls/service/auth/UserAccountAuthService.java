package ca.mcgill.ecse321.opls.service.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.auth.OAuthHelper;
import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import jakarta.transaction.Transactional;

/**
 * Service to interact with user accounts.
 */
@Service
public class UserAccountAuthService {

	@Autowired
	private UserAccountRepository userRepository;

	@Autowired
	private UserAccountClaimRepository claimRepository;

	/**
	 * Try to authenticate a user with resource credentials.
	 * 
	 * @param username
	 *            The username for the account.
	 * @param password
	 *            The password for the account.
	 * @return The user object, if the authentication is successful.
	 * @throws OplsApiException
	 *             if authentication failed.
	 */
	@Transactional
	public UserAccount authenticateUser(String username, String password) {
		var user = userRepository.findUserAccountByUsername(username);
		if (user == null || !user.isPasswordCorrect(password)) {
			throw OAuthHelper.Errors
					.invalidGrant("Incorrect username or password.");
		}

		return user;
	}

	/**
	 * Get all OAuthClaim values associated with a user.
	 * 
	 * @param user
	 *            The user.
	 * @return The list of claims.
	 */
	@Transactional
	public Collection<OAuthClaim> getUserClaims(UserAccount user) {
		return (List<OAuthClaim>) claimRepository.getUserClaims(user);
	}

}
