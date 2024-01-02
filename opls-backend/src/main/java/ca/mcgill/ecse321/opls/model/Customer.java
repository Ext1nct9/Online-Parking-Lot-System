package ca.mcgill.ecse321.opls.model;

import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

@Entity
public class Customer extends UuidModel {
	@OneToOne(optional = false, fetch = FetchType.EAGER)
	private UserAccount userAccount;

	@Column(nullable = true)
	private String savedLicensePlate;

	@Column(nullable = true)
	private String savedBillingAccountId;

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public String getSavedLicensePlate() {
		return savedLicensePlate;
	}

	public void setSavedLicensePlate(String savedLicensePlate) {
		this.savedLicensePlate = savedLicensePlate;
	}

	public String getSavedBillingAccountId() {
		return savedBillingAccountId;
	}

	public void setSavedBillingAccountId(String savedBillingAccountId) {
		this.savedBillingAccountId = savedBillingAccountId;
	}
}
