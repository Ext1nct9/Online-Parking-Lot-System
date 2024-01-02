package ca.mcgill.ecse321.opls.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.OplsStartupService;
import ca.mcgill.ecse321.opls.dto.account.ResetPasswordRequestDto;
import ca.mcgill.ecse321.opls.dto.account.ResetSecurityQuestionAnswerRequestDto;
import ca.mcgill.ecse321.opls.dto.account.SecurityQuestionResponseDto;
import ca.mcgill.ecse321.opls.dto.account.UpdateUserAccountRequestDto;
import ca.mcgill.ecse321.opls.dto.account.UserAccountResponseDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import ca.mcgill.ecse321.opls.service.UserAccountService;
import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestUserAccountController extends OplsApiTester{

	private static final String username = "Ordinateur",
			password = "Password123", firstName = "Comp", lastName = "Youter",
			securityQuestion = "Multivac, what do you yourself want more than anything else?",
			securityQuestionAnswer = "Patate frites";

	@Autowired
	private UserAccountRepository userRepo;
	
	@Autowired
	private UserAccountService userService;

	
	private UserAccount userAccount;

	@Autowired
	private OplsStartupService startupService;
	
	@Override
	@PostConstruct
	public void specifyAuthentication() {
		this.addDefaultClaim(OAuthClaim.ADMIN);
		this.addDefaultClaim(OAuthClaim.EMPLOYEE);
		this.addDefaultClaim(OAuthClaim.CUSTOMER);
	}

	@Override
	@BeforeEach
	public void setupTestData() {
		userAccount = userService.createUserAccount(username, firstName, lastName, password, securityQuestion, securityQuestionAnswer);
	}
	
    @AfterEach
    public void clearDatabase() throws Exception {
        startupService.startupTest();
    }

    @BeforeEach
    public void setupData() {
        startupService.initializeConfiguration();
        startupService.initializeParkingLotStructure();
    }


	private void assertDtoEqual(UserAccount user, UserAccountResponseDto response) {
		assertNotNull(response);
		assertEquals(user.getUuid(), response.uuid);
		assertEquals(user.getUsername(), response.username);
		assertEquals(user.getFirstName(), response.firstname);
		assertEquals(user.getLastName(), response.lastname);
		assertEquals(user.getSecurityQuestion(), response.securityQuestion); 
	}
	
	/**
	 * Test end point GET /account/{uuid}
	 */
	@Test
	public void testGetUserAccount(){
		//test valid result

		var endpoint = "/account/" + userAccount.getUuid().toString();
		var badEndpoint = "/account/"+ UUID.randomUUID().toString();
		UserAccountResponseDto response = this.exchange(HttpMethod.GET, endpoint , UserAccountResponseDto.class, HttpStatus.OK).getBody();

		assertNotNull(response);
		assertEquals(userAccount.getUuid(), response.uuid);
		assertEquals(userAccount.getUsername(), response.username);
		assertEquals(userAccount.getFirstName(), response.firstname);
		assertEquals(userAccount.getLastName(), response.lastname);
		assertEquals(userAccount.getSecurityQuestion(), response.securityQuestion);
		
		// test invalid request with invalid UUID
		this.assertReturnsError(HttpStatus.NOT_FOUND, "not_found", "the user account with the given UUID does not exist!", HttpMethod.GET, badEndpoint, null);
	}
	/**
	 * Test end point GET /account
	 */
	@Test
	public void testGetUserAccountSelf(){
		//test valid result
		UserAccountResponseDto response = this.exchange(HttpMethod.GET, "/account", UserAccountResponseDto.class, HttpStatus.OK).
				getBody();
		var userAccount2 = this.getUserAccount();		
		assertDtoEqual(userAccount2, response);
	}
	
	/**
	 * Test end point PUT /account
	 */
	@Test
	public void testUpdateUserAccount(){
		// test valid result
		var endpoint = "/account";
		var newUsername = "Ilovecats";
		var newFirstName = "Kevin";
		var newLastName = "Nguyen";
		UpdateUserAccountRequestDto request = new UpdateUserAccountRequestDto(newUsername, newFirstName, newLastName);
		var response = this.exchange(HttpMethod.PUT, endpoint, request, UserAccountResponseDto.class, HttpStatus.OK).getBody();
		assertNotNull(response);
		assertEquals(newUsername, response.username);
		assertEquals(newFirstName, response.firstname);
		assertEquals(newLastName, response.lastname);

		// test invalid result
		UpdateUserAccountRequestDto request2 = new UpdateUserAccountRequestDto(username, firstName, lastName);
		this.assertReturnsError(HttpStatus.BAD_REQUEST, "invalid_request", "The username is already taken", HttpMethod.PUT, endpoint, request2, null);
	}

	/**
	 * Test end point GET /account/{username}/resetPassword/
	 */
	@Test
	public void testGetSecurityQuestion(){
		// test valid request
		var endpoint = "/account/"+userAccount.getUsername()+"/resetPassword";
		var response = this.exchange(HttpMethod.GET, endpoint, SecurityQuestionResponseDto.class, HttpStatus.OK).getBody();
		assertNotNull(response);
		assertEquals(userAccount.getSecurityQuestion(), response.securityQuestion);
	}

	/**
	 * Test end point POST /account/{username}/resetPassword
	 */
	@Test
	public void testChangePassword(){
		// test valid request\
		/** */
		var newPassword = "123456789";

		ResetPasswordRequestDto  request = new ResetPasswordRequestDto(newPassword, securityQuestionAnswer);
		var endpoint = "/account/"+userAccount.getUsername()+"/resetPassword";
		
		UserAccountResponseDto response = this.exchange(HttpMethod.POST, endpoint, request, UserAccountResponseDto.class, HttpStatus.OK).getBody();
		var responseCheck = userRepo.findUserAccountById(userAccount.getId());
		assertNotNull(response);
		assertEquals(userAccount.getUuid(), response.uuid);
		assertEquals(userAccount.getUsername(), response.username);
		assertEquals(userAccount.getFirstName(), response.firstname);
		assertEquals(userAccount.getLastName(), response.lastname);
		assertTrue(responseCheck.isPasswordCorrect(newPassword));
		assertTrue(responseCheck.isSecurityAnswerCorrect(securityQuestion, securityQuestionAnswer ));
		
		// No assert returns error because there would not be a resetPassword tab anyways if the username was not right!
	}
	
	/**
	 * Test end point PUT /account/security/
	 */
	@Test
	public void testChangeSecurityQnA(){
		// test valid request
		// Tester info (default user to test on)
		var testerPassword = "pw1234567";
		var newSecurityQ = "Hot";
		var newSecurityA = "Dog";

		ResetSecurityQuestionAnswerRequestDto  request = new ResetSecurityQuestionAnswerRequestDto (testerPassword, newSecurityQ, newSecurityA); // brute forced! I spent 3 hours figuring out that the tester password doesnt fit in the constraints of the request DTO...
		ResetSecurityQuestionAnswerRequestDto  badRequest = new ResetSecurityQuestionAnswerRequestDto ("wrongpass", newSecurityQ, newSecurityA);
		
		var endpoint = "/account/security";
		UserAccountResponseDto response = this.exchange(HttpMethod.PUT, endpoint, request, UserAccountResponseDto.class, HttpStatus.OK).getBody();
		var responseCheck = userRepo.findUserAccountByUsername("tester");
		assertNotNull(response);
		assertEquals(responseCheck.getUuid(), response.uuid);
		assertEquals(responseCheck.getUsername(), response.username);
		assertEquals(responseCheck.getFirstName(), response.firstname);
		assertEquals(responseCheck.getLastName(), response.lastname);
		assertTrue(responseCheck.isPasswordCorrect(testerPassword));
		assertTrue(responseCheck.isSecurityAnswerCorrect(newSecurityQ, newSecurityA ));

		// invalid request
		this.assertReturnsError(HttpStatus.UNAUTHORIZED, "incorrect_password", "the password for this user account is incorrect", HttpMethod.PUT, endpoint, badRequest, null);		
	}

}
