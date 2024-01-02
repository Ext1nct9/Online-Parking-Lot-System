package ca.mcgill.ecse321.opls.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.auth.UserAccount;

/**
 * Repository to interface with the user_account table.
 */
public interface UserAccountRepository extends CrudRepository<UserAccount, Integer> {
	
	/** Find a user account by its numerical id. */
	UserAccount findUserAccountById(int id);

	/** Find a user account by its username. */
	UserAccount findUserAccountByUsername(String username);

	/** Find a user account by its UUID. */
	UserAccount findUserAccountByUuid(UUID uuid);

}
