package ca.mcgill.ecse321.opls.service;

import static java.util.Objects.isNull;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.CustomerRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;

@Service
public class CustomerService {

	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private UserAccountClaimRepository claimRepository;
	
	/**
	 *  Retrieves a customer with given uuid.
	 * @param uuid	The uuid of the customer searchee
	 * @return		the customer with corresponding uuid
	 */
	public Customer getCustomer(UUID uuid) {
		Customer c = customerRepository.findCustomerByUuid(uuid);
		if (isNull(c)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Customer not found.");
		}
		return c;
	}

	/**
	 * Retrieves a customer by its associated userAccountId 
	 * @param userAccountId		The possibly requested userAccount Id
	 * @return	The associated customer to Id
	 */
	public Customer getCustomerByUAId(int userAccountId) {
		Customer c = customerRepository.findCustomerByUserAccountId(userAccountId);
		if (isNull(c)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Customer not found.");
		}
		return c;
	}
	
	/**
	 * Retrieves a customer by its customerId 
	 * @param customerId		The possibly requested customer Id
	 * @return					the customer with that id
	 */
	public Customer getCustomerByCustomerId(int customerId) {
		Customer c = customerRepository.findCustomerById(customerId);	
		if (isNull(c)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Customer not found.");
		}
		return c;	
	}

	/**
	 * Retrieves a customer based on a specific user account.
	 * @param userAccount	The user account associated to the customer
	 * @return	The requested customer 
	 */
	public Customer getCustomer(UserAccount userAccount) {
		Customer c = customerRepository.findCustomerByUserAccount(userAccount);
		if (isNull(c)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Customer not found.");
		}
		return c;	
	}
	
	/**
	 * Associate a user account and a customer.
	 * 
	 * @param ua
	 *            The UserAccount entity.
	 * @param customer
	 *            The Customer entity.
	 */
	private void associateUserWithCustomer(UserAccount ua, Customer customer) {
		Customer existingCustomer = null;
		try {
			existingCustomer = getCustomerByUAId(ua.getId());
		} catch (Exception e) {
		}

		if (existingCustomer != null) {
			throw new OplsApiException(HttpStatus.CONFLICT,
					"A customer is already associated with this user account.");
		}

		if (!claimRepository.userHasClaim(ua, OAuthClaim.CUSTOMER)) {
			claimRepository.save(ua.addUserAccountClaim(OAuthClaim.CUSTOMER));
		}

		customer.setUserAccount(ua);
	}

	/**
	 * Creates a customer based on required parameters.
	 * @param ua				Associated user account
	 * @param savedLicensePlate	saved license plate to be linked to the customer
	 * @param billingAccountId	billing account Id to be linked to the customer.
	 * @return The created customer.
	 */
	@Transactional
	public Customer createCustomer(UserAccount ua, String savedLicensePlate, String billingAccountId) {
		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request", "the user account is null!");
		}
		
		Customer c = new Customer();
		associateUserWithCustomer(ua, c);
		c.setSavedLicensePlate(savedLicensePlate);
		c.setSavedBillingAccountId(billingAccountId);
		return customerRepository.save(c);
	}
	
	/**
	 * Update a customer's information.
	 * 
	 * @param customer			The customer entity to update.
	 * @param newLicensePlate	The new license plate.
	 * @param billingAccountId	The new billing account.
	 * @return					The customer.
	 */
	@Transactional
	public Customer updateCustomer(Customer customer, String newLicensePlate, String billingAccountId) {
		if (newLicensePlate != null) {
			customer.setSavedLicensePlate(newLicensePlate);
		}
		
		if (billingAccountId != null) {
			customer.setSavedBillingAccountId(billingAccountId);
		}
		
		return customerRepository.save(customer);
	}
	
	/**
	 * Set a customer's license exclusively
	 * @param uuid					The uuid of the customer
	 * @param newLicensePlate		The new license plate 
	 * @return						The updated customer
	 */
	@Transactional
	public Customer setCustomerLicense(UUID uuid, String newLicensePlate) {
		Customer c = this.getCustomer(uuid);
		c.setSavedLicensePlate(newLicensePlate);
		return customerRepository.save(c);
	}
	
	/**
	 * Set a customer's billing account id exclusively
	 * @param uuid					The uuid of the customer
	 * @param billingAccountId		the new billing account id
	 * @return						the updated customer
	 */
	@Transactional
	public Customer setCustomerBilling(UUID uuid, String billingAccountId) {
		Customer c = this.getCustomer(uuid);
		c.setSavedBillingAccountId(billingAccountId);
		return customerRepository.save(c);
	}

	
	/**
	 * Set a customer's user account
	 * @param newUA		The new associated user acount
	 * @param uuid		The uuid of the customer
	 * @return			the updated customer
	 */
	@Transactional
	public Customer setCustomerUserAccount(UserAccount newUA, UUID uuid) {
		if (isNull(newUA)) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request", "the user account is null!");
		}

		Customer c = this.getCustomer(uuid);
		associateUserWithCustomer(newUA, c);
		return customerRepository.save(c);
	}

	/**
	 * Delete the customer
	 * @param customer		The customer
	 */
	@Transactional
	public Customer deleteCustomer(Customer customer) {
		Customer fetchedCust = customerRepository.findCustomerById(customer.getId());
		customerRepository.delete(customer);
		return fetchedCust;
	}

}
