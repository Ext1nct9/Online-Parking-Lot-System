package ca.mcgill.ecse321.opls.dto;

import java.util.UUID;

import ca.mcgill.ecse321.opls.model.Customer;
import jakarta.validation.constraints.Size;

/**
 * Request and response model for a customer.
 */
public class CustomerDto {

	/**
	 * The saved license plate of the customer.
	 */
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String savedLicensePlate;

	/**
	 * The billing account id of the customer.
	 */
	@Size(min = 1, message = "The field must be at least 1 character!")
	public String billingAccountId;
	
	/**
	 * The uuid of the associated user account.
	 */
	public UUID userUuid;

	/** Default constructor. */
	public CustomerDto() {
	};

	/** Constructor with fields. */
	public CustomerDto(String savedLicensePlate, String billingAccountId) {
		this.billingAccountId = billingAccountId;
		this.savedLicensePlate = savedLicensePlate;
	}

	/** Constructor from database entity. */
	public CustomerDto(Customer customer) {
		this.billingAccountId = customer.getSavedBillingAccountId();
		this.savedLicensePlate = customer.getSavedLicensePlate();
	}
	
	/**Constructor with fields **/
	public CustomerDto(UUID userUuid, String savedLicensePlate, String billingAccountId) {
		this.userUuid = userUuid;
		this.billingAccountId = billingAccountId;
		this.savedLicensePlate = savedLicensePlate;
	}

}
