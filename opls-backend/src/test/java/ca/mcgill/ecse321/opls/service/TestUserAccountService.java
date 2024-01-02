package ca.mcgill.ecse321.opls.service;


import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
public class TestUserAccountService {

	@Mock
	private UserAccountRepository userAccountDao;

	@InjectMocks
	private UserAccountService userAccountService;

	private static final String not_username = "weinerman", username = "n64", password = "nuts",
			firstName = "banjo", lastName = "kazooie", securityQuestion = "what animal are you?",
			securityQuestionAnswer = "same beast";

	private static final UUID USERACCOUNT_UUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f6"),
			NOT_USERACCOUNT_UUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f7");

	private static final int USERACCOUNT_ID = 1234, NOT_USERACCOUNT_ID = 5678;

	private UserAccount userAccount;

	@BeforeEach
	public void setupMocks() {

		userAccount = new UserAccount();
		userAccount.setUsername(username);
		userAccount.setFirstName(firstName);
		userAccount.setLastName(lastName);

		UserAccount[] accounts = new UserAccount[1];
		accounts[0] = userAccount;


		try {
			userAccount.setPassword(password);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		userAccount.setSecurityAnswer(securityQuestion, securityQuestionAnswer);


		lenient().when(userAccountDao.findUserAccountByUuid(USERACCOUNT_UUID)).
		thenAnswer((InvocationOnMock invocation) -> accounts[0]);

		lenient().when(userAccountDao.findUserAccountByUuid(NOT_USERACCOUNT_UUID)).
		thenAnswer((InvocationOnMock invocation) -> null);

		lenient().when(userAccountDao.findUserAccountById(USERACCOUNT_ID)).
		thenAnswer((InvocationOnMock invocation) -> accounts[0]);

		lenient().when(userAccountDao.findUserAccountById(NOT_USERACCOUNT_ID)).
		thenAnswer((InvocationOnMock invocation) -> null);

		lenient().when(userAccountDao.findUserAccountByUsername(username)).
		thenAnswer((InvocationOnMock invocation) -> accounts[0]);

		lenient().when(userAccountDao.findUserAccountByUsername(not_username)).
		thenAnswer((InvocationOnMock invocation) -> null);

		lenient().when(userAccountDao.save(any())).
		thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

		lenient().doAnswer((InvocationOnMock invocation) -> accounts[0] = null).
		when(userAccountDao).delete(userAccount);
	}
	@Test
	public void testGetUserAccount() {
		//test valid request using uuid
		UserAccount ua1 = userAccountService.getUserAccount(USERACCOUNT_UUID);
		assertNotNull(ua1);
		assertEquals(username, ua1.getUsername());
		assertEquals(firstName, ua1.getFirstName());
		assertEquals(lastName, ua1.getLastName());
		assertEquals(securityQuestion, ua1.getSecurityQuestion());
		assertTrue(ua1.isPasswordCorrect(password));
		assertTrue(ua1.isSecurityAnswerCorrect(securityQuestion, securityQuestionAnswer));

		//test invalid request using uuid
		assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
				() ->   userAccountService.getUserAccount(NOT_USERACCOUNT_UUID));

		//test valid request using numerical account id
		UserAccount ua3 = userAccountService.getUserAccount(USERACCOUNT_ID);
		assertNotNull(ua3);
		assertEquals(username, ua3.getUsername());
		assertEquals(firstName, ua3.getFirstName());
		assertEquals(lastName, ua3.getLastName());
		assertEquals(securityQuestion, ua3.getSecurityQuestion());
		assertTrue(ua3.isPasswordCorrect(password));
		assertTrue(ua3.isSecurityAnswerCorrect(securityQuestion, securityQuestionAnswer));

		//test invalid request using numerical account id

		assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
				() ->   userAccountService.getUserAccount(NOT_USERACCOUNT_ID));

		//test valid request using username
		UserAccount ua5 = userAccountService.getUserAccount(username);
		assertNotNull(ua5);
		assertEquals(username, ua5.getUsername());
		assertEquals(firstName, ua5.getFirstName());
		assertEquals(lastName, ua5.getLastName());
		assertEquals(securityQuestion, ua5.getSecurityQuestion());
		assertTrue(ua5.isPasswordCorrect(password));
		assertTrue(ua5.isSecurityAnswerCorrect(securityQuestion, securityQuestionAnswer));

		//test invalid request using numerical account id

		assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
				() ->  userAccountService.getUserAccount(not_username)); 
	}

	@Test
	public void testCreateUserAccount() {
		//need too lenient definition for save 
		try {
			//test valid request
			UserAccount ua1 = userAccountService.createUserAccount("frank", firstName, lastName, password, securityQuestion, securityQuestionAnswer);
			assertNotNull(ua1);
			assertEquals("frank", ua1.getUsername());
			assertEquals(firstName, ua1.getFirstName());
			assertEquals(lastName, ua1.getLastName());
			assertEquals(securityQuestion, ua1.getSecurityQuestion());
			assertTrue(ua1.isPasswordCorrect(password));
			assertTrue(ua1.isSecurityAnswerCorrect(securityQuestion, securityQuestionAnswer));

			//test invalid request (non unique username, since username acc is already saved)

			assertNotNull(userAccountService.getUserAccount(username));
			assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
					() ->  userAccountService.createUserAccount(username, firstName, lastName, password, 
							securityQuestion, securityQuestionAnswer));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	@Test
	public void testUpdateUserAccountSecurity() {
		//test valid request(correct password, different security answer)
		userAccountService.updateSecurityQandA(userAccount, password, securityQuestion, "bear");
		UserAccount ua1 = userAccountService.getUserAccount(username);
		assertNotNull(ua1);
		assertEquals(username, ua1.getUsername());
		assertEquals(firstName, ua1.getFirstName());
		assertEquals(lastName, ua1.getLastName());
		assertEquals(securityQuestion, ua1.getSecurityQuestion());
		assertTrue(ua1.isPasswordCorrect(password));
		assertTrue(ua1.isSecurityAnswerCorrect(securityQuestion, "bear"));

		//test valid request (different security question and answer)
		userAccountService.updateSecurityQandA(userAccount, password, "regular show?", "rigby");
		UserAccount ua2 = userAccountService.getUserAccount(username);
		assertNotNull(ua2);
		assertEquals(username, ua2.getUsername());
		assertEquals(firstName, ua2.getFirstName());
		assertEquals(lastName, ua2.getLastName());
		assertEquals("regular show?", ua2.getSecurityQuestion());
		assertTrue(ua2.isPasswordCorrect(password));
		assertTrue(ua2.isSecurityAnswerCorrect("regular show?", "rigby"));

		//test invalid request (incorrect password)
		assertNotNull(userAccountService.getUserAccount(username));
		assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
				() ->  userAccountService.createUserAccount(username, firstName, lastName, "weinerballs", 
						securityQuestion, securityQuestionAnswer));

	}

	@Test
	public void testUpdateUserAccountPassword() {
		//test valid request(correct password, different security answer)
		try {
			String newpass1 = "BBL5";
			userAccountService.updatePassword(userAccount, securityQuestion, securityQuestionAnswer, newpass1);
			UserAccount ua1 = userAccountService.getUserAccount(username);
			assertNotNull(ua1);
			assertEquals(username, ua1.getUsername());
			assertEquals(firstName, ua1.getFirstName());
			assertEquals(lastName, ua1.getLastName());
			assertEquals(securityQuestion, ua1.getSecurityQuestion());
			assertTrue(ua1.isPasswordCorrect(newpass1));
			assertTrue(ua1.isSecurityAnswerCorrect(securityQuestion, securityQuestionAnswer));

			//test invalid request (incorrect security answer)
			assertNotNull(userAccountService.getUserAccount(username));
			assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
					() ->  userAccountService.createUserAccount(username, firstName, lastName, password, 
							securityQuestion, "shit"));

			//test invalid request (incorrect security question)
			assertNotNull(userAccountService.getUserAccount(username));
			assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
					() ->  userAccountService.createUserAccount(username, firstName, lastName, password, 
							"shit", securityQuestionAnswer));

			//test invalid request (incorrect security question and answer)
			assertNotNull(userAccountService.getUserAccount(username));
			assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
					() ->  userAccountService.createUserAccount(username, firstName, lastName, password, 
							"shit", "poop!"));

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}


}
