package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.auth.UserAccount;

@SpringBootTest
public class TestUserAccountRepository {
	
	public static UserAccount newAccount(String username, String firstName, String lastName, String password, String securityQuestion, String securityAnswer) {
		var user = new UserAccount();
		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		try {
			user.setPassword(password);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		user.setSecurityAnswer(securityQuestion, securityAnswer);
		
		return user;
	}

	@Autowired
	private UserAccountRepository userAccountRepository;

	@AfterEach
	public void clearDatabase() {
		userAccountRepository.deleteAll();
	}

	/**
	 * Test the creation of a user account and reading its values.
	 */
	@Test
	public void testCreation() {
		// create user account
		var user = userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));

		// validate retrieved user account
		var result = userAccountRepository.findUserAccountByUsername(user.getUsername());
		assertNotNull(result);
		assertEquals(user.getFirstName(), result.getFirstName());
		assertEquals(user.getLastName(), result.getLastName());
	}

	/**
	 * Test updating a user account's fields
	 */
	@Test
	public void testUpdate() {
		// create user account
		var user = userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));
		int id = user.getId();

		// validate user account fields
		var result = userAccountRepository.findUserAccountById(id);
		assertNotNull(result);
		assertEquals(user.getUsername(), result.getUsername());
		assertEquals(user.getFirstName(), result.getFirstName());
		assertEquals(user.getLastName(), result.getLastName());

		// update user account fields
		String username = "squidward";
		String firstName = "spongebob";
		String lastName = "squarepants";
		result.setUsername(username);
		result.setFirstName(firstName);
		result.setLastName(lastName);
		result = userAccountRepository.save(result);

		// validate updated fields
		var result1 = userAccountRepository.findUserAccountById(id);
		assertNotNull(result1);
		assertEquals(username, result1.getUsername());
		assertEquals(firstName, result1.getFirstName());
		assertEquals(lastName, result1.getLastName());
	}

	/**
	 * Test deleting a user account
	 */
	@Test
	public void testDelete() {
		// create user account
		var user = userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));
		int id = user.getId();

		// validate user account fields
		var result = userAccountRepository.findUserAccountById(id);
		assertNotNull(result);
		assertEquals(user.getUsername(), result.getUsername());
		assertEquals(user.getFirstName(), result.getFirstName());
		assertEquals(user.getLastName(), result.getLastName());

		// delete user account
		userAccountRepository.delete(result);

		// try to get user account again
		var result1 = userAccountRepository.findUserAccountById(id);
		assertNull(result1);
	}

	/**
	 * Ensure that the system will prevent duplicate usernames.
	 */
	@Test
	public void testUniqueUsername() {
		// create user account with unique username
		var user1 = userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));

		var result = userAccountRepository.findUserAccountByUsername("john");
		assertNotNull(result);

		// try to save the repeat username
		try {
			userAccountRepository.save(newAccount("john", "John2", "Appleseed2", "password", "Where are you happiest in the world?", "I don't know."));
			fail("Able to create multiple accounts with the same username.");
		} catch (Exception e) {
		}

		// ensure first user still saved
		result = userAccountRepository.findUserAccountByUsername("john");
		assertNotNull(result);
		assertEquals(user1.getFirstName(), result.getFirstName());
	}

	/**
	 * Test the password functionality from account creation to logging in.
	 */
	@Test
	public void testLogin() {
		// create user
		userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));

		// login request
		var reqUsername = "john";
		var reqPassword = "password";
		var result = userAccountRepository
				.findUserAccountByUsername(reqUsername);
		assertNotNull(result);
		assertEquals(reqUsername, result.getUsername());
		assertTrue(result.isPasswordCorrect(reqPassword));

		// test incorrect password
		assertFalse(result.isPasswordCorrect("wrong_password"));
	}

	/**
	 * Test the security question functionality, from creation to validation.
	 */
	@Test
	public void validateSecurityQuestion() {
		// create user
		userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));

		// security answer request
		var reqUsername = "john";
		var reqSecurityQuestion = "Where are you happiest in the world?";
		var reqSecurityAnswer = "I don't know";

		// validate
		var result = userAccountRepository
				.findUserAccountByUsername(reqUsername);
		assertNotNull(result);
		assertEquals(reqUsername, result.getUsername());
		assertTrue(result.isSecurityAnswerCorrect(reqSecurityQuestion,
				reqSecurityAnswer));
		
		// validate alternate answer
		assertTrue(result.isSecurityAnswerCorrect(reqSecurityQuestion, "i dont know"));
		
		// validate alternate answer
		assertTrue(result.isSecurityAnswerCorrect(reqSecurityQuestion, "i don-t know"));
		
		// validate alternate answer
		assertTrue(result.isSecurityAnswerCorrect(reqSecurityQuestion, "idontknow"));
	}

	/**
	 * Test reset password flow.
	 */
	@Test
	public void testResetPassword() {
		// create user
		var user = userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));

		// do not allow set password without the security answer
		try {
			user.setPassword("password2");
			fail("Able to set password without the security question.");
		} catch (Exception e) {
		}
		
		// allow reset password with security answer
		try {
			user.setPassword("password2", "Where are you happiest in the world?", "I don't know");
		} catch (Exception e) {
			fail(e);
		}

		// ensure that new password is correct
		assertTrue(user.isPasswordCorrect("password2"));
	}

	/**
	 * Test reset security question and answer
	 */
	@Test
	public void testResetSecurityQuestionAndAnswer() {
		// create user
		userAccountRepository.save(newAccount("john", "John", "Appleseed", "password", "Where are you happiest in the world?", "I don't know."));

		// security answer request
		var reqUsername = "john";
		var reqSecurityQuestion = "Where are you happiest in the world?";
		var reqSecurityAnswer = "I don't know";

		// validate
		var result = userAccountRepository.findUserAccountByUsername(reqUsername);
		assertNotNull(result);
		assertEquals(reqUsername, result.getUsername());
		assertTrue(result.isSecurityAnswerCorrect(reqSecurityQuestion, reqSecurityAnswer));

		// update security question and answer
		String newSecurityQuestion = "have you started the comp 251 proof project yet?";
		String newSecurityAnswer = "absolutely not :D";
		result.setSecurityAnswer(newSecurityQuestion, newSecurityAnswer);
		userAccountRepository.save(result);

		// validate new security question and answer
		var result1 = userAccountRepository.findUserAccountByUsername(reqUsername);
		assertNotNull(result1);
		assertEquals(reqUsername, result1.getUsername());
		assertTrue(result1.isSecurityAnswerCorrect(newSecurityQuestion, newSecurityAnswer));
	}
}
