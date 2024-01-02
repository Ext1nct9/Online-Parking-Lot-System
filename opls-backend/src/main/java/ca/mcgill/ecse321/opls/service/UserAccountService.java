package ca.mcgill.ecse321.opls.service;

import static java.util.Objects.isNull;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;

@Service
public class UserAccountService {
	@Autowired
	UserAccountRepository userAccountRepository;
	
	/**
	 * Retrieves the userAccount with the given numerical id.
	 * @param id      numerical ID of the userAccount to get
	 * @return        userAccount with the given numerical id
	 */
	public UserAccount getUserAccount(int userAccountId) {
		UserAccount ua = userAccountRepository.findUserAccountById(userAccountId);
		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
					"the user account with the given UUID does not exist!");
		}
		return ua;
	}

	/**
	 * Retrieves the userAccount with the given UUID.
	 * @param uuid     The UUID of the userAccount to get
	 * @return        userAccount with the given UUID
	 */
	@Transactional
	public UserAccount getUserAccount(UUID uuid) {
		UserAccount ua =userAccountRepository.findUserAccountByUuid(uuid);

		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
					"the user account with the given UUID does not exist!");
		}

		return ua;
	}

	/**
	 * Retrieves the userAccount with the given username.
	 * @param username      username of the userAccount to get
	 * @return        		userAccount with the given username
	 */
	@Transactional
	public UserAccount getUserAccount(String username) {
		UserAccount ua = userAccountRepository.findUserAccountByUsername(username);
		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
					"the user account with the given username does not exist!");
		}
		return ua;
	}

	/**
	 * Creates user account in database with given info
	 * @param userName 				User's chosen username
	 * @param fName					User's real first name
	 * @param lName					User's real last name
	 * @param password				password to log in 
	 * @param securityQuestion		question user will be asked if password forgotten
	 * @param securityAnswer 		answer to security question
	 * @return 						new user account ua
	 * @throws Exception 			impossible exception for creation, wont occur
	 */
	@Transactional
	public UserAccount createUserAccount(String userName, String fName, String lName, 
			String password, String securityQuestion, String securityAnswer) {

		if (!isNull(userAccountRepository.findUserAccountByUsername(userName))) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request", "The username is already taken");
		}
		UserAccount ua = new UserAccount();
	
		ua.setUsername(userName);
		ua.setFirstName(fName);
		ua.setLastName(lName);
		try {
			ua.setPassword(password);
		} catch (Exception e) {
			throw new OplsApiException(HttpStatus.UNAUTHORIZED, "Not authorized to change password.");
		}
		ua.setSecurityAnswer(securityQuestion, securityAnswer);

		return userAccountRepository.save(ua);
	}
	
	/**
	 * Update a user account.
	 * @param uuid The uuid of the user account.
	 * @param username The new username.
	 * @param firstName The first name.
	 * @param lastName The last name.
	 * @return The updated user account entity.
	 */
	@Transactional
	public UserAccount updateUserAccount(UUID uuid, String username, String firstName, String lastName) {
		UserAccount ua = userAccountRepository.findUserAccountByUuid(uuid);
		UserAccount ua2 = userAccountRepository.findUserAccountByUsername(username);
		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
					"the user account with the given UUID does not exist!");
		}
		if (ua2 != null && ua != ua2) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request", "The username is already taken");
		}
		
		ua.setUsername(username);
		ua.setFirstName(firstName);
		ua.setLastName(lastName);
		
		return userAccountRepository.save(ua);
	}
	
	/**
	 * Updates security question and answer for a user account
	 * @param ua					The given user account
	 * @param password				the user account's supposed password
	 * @param newSecurityQuestion	the new security question for the ua
	 * @param newSecurityAnswer		the new security answer for the ua
	 * @return						user account with (possibly) updated params
	 */
	@Transactional
	public UserAccount updateSecurityQandA(UserAccount ua, String password, String newSecurityQuestion, String newSecurityAnswer) {
		if (!ua.isPasswordCorrect(password)) {
			throw new OplsApiException(HttpStatus.UNAUTHORIZED, "incorrect_password", "the password for this user account is incorrect");
		}
		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request", "the user account is null!");
		}
		ua.setSecurityAnswer(newSecurityQuestion, newSecurityAnswer);
		userAccountRepository.save(ua);
		
		return ua;
	}
	
	/**
	 * Updates password of a given user account 
	 * @param ua				Given user account
	 * @param securityQuestion	Security question associated to the user account
	 * @param securityAnswer	answer to the security question
	 * @param newPassword		new password for the user account
	 * @return					the updated useraccount
	 * @throws Exception		invalid if incorrect Q or A.
	 */
	public UserAccount updatePassword(UserAccount ua, String securityQuestion, String securityAnswer, String newPassword) {
		if (isNull(ua)) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request", "the user account is null!");
		}
		try {
			ua.setPassword(newPassword, securityQuestion, securityAnswer);
		} catch (Exception e) {
			throw new OplsApiException(HttpStatus.UNAUTHORIZED, "Not authorized to change password.");
		}
		
		userAccountRepository.save(ua);
		return ua;
	}
	
	







}
