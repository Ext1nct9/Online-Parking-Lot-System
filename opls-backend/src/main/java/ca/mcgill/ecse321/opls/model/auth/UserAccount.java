package ca.mcgill.ecse321.opls.model.auth;

import java.util.Set;

import ca.mcgill.ecse321.opls.auth.HashHelper;
import ca.mcgill.ecse321.opls.auth.HashHelper.HashResult;
import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.UuidModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

/**
 * User account model.
 */
@Entity
public class UserAccount extends UuidModel {
	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false, length = HashHelper.HASH_LEN_ENCODED)
	private String passwordHash;

	@Column(nullable = false, length = HashHelper.HASH_SALT_LEN_ENCODED)
	private String passwordHashSalt;

	@Column(nullable = false)
	private String securityQuestion;

	@Column(nullable = false, length = HashHelper.HASH_LEN_ENCODED)
	private String securityAnswerHash;

	@Column(nullable = false, length = HashHelper.HASH_SALT_LEN_ENCODED)
	private String securityAnswerHashSalt;

	@OneToMany(mappedBy = "userAccount", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<OAuthClientSession> sessions;

	@OneToMany(mappedBy = "id.userAccount", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<UserAccountClaim> claims;

	@OneToOne(optional = true, fetch = FetchType.EAGER, mappedBy = "userAccount", cascade = CascadeType.REMOVE)
	private Customer customer;

	@OneToOne(optional = true, fetch = FetchType.EAGER, mappedBy = "userAccount", cascade = CascadeType.REMOVE)
	private Employee employee;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Set the hashed password in the database.
	 * 
	 * @param password
	 *            The user's password to be hashed.
	 * @throws Exception
	 *             if the user has a security question and answer.
	 */
	public void setPassword(String password) throws Exception {
		setPassword(password, null, null);
	}

	/**
	 * Sets the password of the user, validating the security question and
	 * answer if it exists for the user.
	 * 
	 * @param password
	 *            The user's password.
	 * @param securityQuestion
	 *            The user's security question.
	 * @param securityAnswer
	 *            The user's security question answer.
	 * @throws Exception
	 *             if the security answer is incorrect and the user has a saved
	 *             security question.
	 */
	public void setPassword(String password, String securityQuestion,
			String securityAnswer) throws Exception {
		if (this.securityQuestion != null
				&& this.securityQuestion.length() > 0) {
			// validate security question and answer
			if (!isSecurityAnswerCorrect(securityQuestion, securityAnswer)) {
				throw new Exception(
						"Incorrect answer to the security question.");
			}
		}

		HashResult res = HashHelper.hashData(password);
		this.passwordHash = res.getHashedData();
		this.passwordHashSalt = res.getSalt();
	}

	/**
	 * Check if the requested password matches the user.
	 * 
	 * @param password
	 *            The user's password from the request.
	 * @return Whether the password matches the database.
	 */
	public boolean isPasswordCorrect(String password) {
		return HashHelper.testHash(password, this.passwordHashSalt,
				this.passwordHash);
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	private static String transformSecurityAnswer(String securityAnswer) {
		return securityAnswer.replaceAll("[ !@#$%^&*()\\-_=+'\",.<>?]", "")
				.toLowerCase();
	}

	/**
	 * Set the hashed security answer in the database.
	 */
	public void setSecurityAnswer(String securityQuestion,
			String securityAnswer) {
		this.securityQuestion = securityQuestion;

		// transform answer
		securityAnswer = transformSecurityAnswer(securityAnswer);

		// hash and store
		HashResult res = HashHelper.hashData(securityAnswer);
		this.securityAnswerHash = res.getHashedData();
		this.securityAnswerHashSalt = res.getSalt();
	}

	public boolean isSecurityAnswerCorrect(String securityQuestion,
			String securityAnswer) {
		if (!this.securityQuestion.equals(securityQuestion)) {
			return false;
		}

		// transform answer
		securityAnswer = transformSecurityAnswer(securityAnswer);

		// hash and test
		return HashHelper.testHash(securityAnswer, this.securityAnswerHashSalt,
				this.securityAnswerHash);
	}

	public UserAccountClaim addUserAccountClaim(OAuthClaim oAuthClaim) {
		return new UserAccountClaim(this, oAuthClaim);
	}

	public Set<UserAccountClaim> getClaims() {
		return claims;
	}
	
	public Set<OAuthClientSession> getSessions() {
		return sessions;
	}

	public Customer getCustomer() {
		return customer;
	}

	/** Get the integer id of the associated customer, -1 if it does not exist. */
	public int getCustomerId() {
		return customer == null ? -1 : customer.getId();
	}

	public Employee getEmployee() {
		return employee;
	}
	
	/** Get the integer id of the associated employee, -1 if it does not exist. */
	public int getEmployeeId() {
		return employee == null ? -1 : employee.getId();
	}
	
}
