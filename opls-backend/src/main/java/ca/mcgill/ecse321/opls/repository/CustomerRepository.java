package ca.mcgill.ecse321.opls.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

/**
 * Repository to interface with the customer table. 
 */
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
	
	/** Find a customer object by their unique id value. */
	Customer findCustomerById(int id);

	/** Find a customer entry by their uuid. */
    Customer findCustomerByUuid(UUID uuid);
    
    /** Find a customer associated with a UserAccount. */
    Customer findCustomerByUserAccount(UserAccount userAccount);
    
    default Customer findCustomerByUserAccountId(int userAccountId) {
    	var userAccount = new UserAccount();
    	userAccount.overrideId(userAccountId);
    	return findCustomerByUserAccount(userAccount);
    }

}
