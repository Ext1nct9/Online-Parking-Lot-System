package ca.mcgill.ecse321.opls.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.dto.account.ResetPasswordRequestDto;
import ca.mcgill.ecse321.opls.dto.account.ResetSecurityQuestionAnswerRequestDto;
import ca.mcgill.ecse321.opls.dto.account.SecurityQuestionResponseDto;
import ca.mcgill.ecse321.opls.dto.account.UpdateUserAccountRequestDto;
import ca.mcgill.ecse321.opls.dto.account.UserAccountRequestDto;
import ca.mcgill.ecse321.opls.dto.account.UserAccountResponseDto;
import ca.mcgill.ecse321.opls.service.UserAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * User account operations.
 */
@RestController
@CrossOrigin
public class UserAccountController {

	@Autowired
	UserAccountService userService;

	/**
	 * Create a user account.
	 * 
	 * @HTTPmethod			POST
	 * @URL					/account
	 * @param token			Bearer access token. No required user claims.
	 * @param request		The user account request.
	 * @return				The created user account.
	 */
	@PostMapping(value = "/account")
	@ResponseBody
	public UserAccountResponseDto createUserAccount(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody UserAccountRequestDto request) {
		AccessTokenHelper.parseAccessToken(token, false, null);

		return new UserAccountResponseDto(
				userService.createUserAccount(request.username,
						request.firstName, request.lastName, request.password,
						request.securityQuestion, request.securityAnswer));
	}

	/**
	 * Retrieve the user account.
	 * 
	 * @HTTPmethod 			GET
	 * @URL					/account/{uuid}
	 * @param token			Bearer access token. Must be registered. No required user claims.
	 * @param uuid			UUID of the user account to find.
	 * @return				The found account.
	 */
	@GetMapping(value = "/account/{uuid}")
	@ResponseBody
	public UserAccountResponseDto getUserAccount(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@NotNull @PathVariable("uuid") UUID uuid) {
		AccessTokenHelper.parseAccessToken(token, true, null);

		return new UserAccountResponseDto(userService.getUserAccount(uuid));
	}

	/**
     * Retrieves a user's own profile using the user account id in the authorization token.
     * 
     * @HTTPmethod        	GET
     * @URL                 /account
     * @param token         Bearer access token. Must be registered. No required user claims.
     * @return              The retrieved account associated with the access token.
     */
	@GetMapping(value = "/account")
	@ResponseBody
	public UserAccountResponseDto getUserAccountSelf(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true, null);

		return new UserAccountResponseDto(
				userService.getUserAccount(credentials.userAccountId));
	}

	/**
	 * Update the general fields of a user's account.
	 * 
	 * @HTTPMethod			PUT
	 * @URL					/account/
	 * @param token			The bearer access token. Must be registered. No required user claims.
	 * @param request		The updated user fields.
	 * @return				The updated user account.
	 */
	@PutMapping(value = "/account")
	@ResponseBody
	public UserAccountResponseDto updateUserAccount(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody UpdateUserAccountRequestDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true, null);
		var user = userService.getUserAccount(credentials.userAccountId);
		var uar = userService.updateUserAccount(user.getUuid(),
				request.username, request.firstName, request.lastName);
		return new UserAccountResponseDto(uar);
	}


	/**
	 * Fetch security question of given account.
	 * 
	 * @HTTPmethod			GET
	 * @URL					/account/{username}/resetPassword
	 * @param token			Bearer access token. No required user claims.
	 * @param username		Path variable account username identifier.
	 * @return				the security question of the account if found, or the UUID of the user.
	 */
	@GetMapping(value = "/account/{username}/resetPassword")
	@ResponseBody
	public SecurityQuestionResponseDto getSecurityQuestion(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@PathVariable("username") String username) {
		AccessTokenHelper.parseAccessToken(token, false, null);
		var SQ = userService.getUserAccount(username).getSecurityQuestion();
		if (SQ != null) {
			return new SecurityQuestionResponseDto(SQ);
		}
		return new SecurityQuestionResponseDto("UUID:"
				+ userService.getUserAccount(username).getUuid().toString());
	}

	/**
	 * Change password of a given account.
	 * 
	 * @HTTPmethod			POST
	 * @URL					/account/{username}/resetPassword
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param username		Path variable account username identifier.
	 * @param request		DTO holding new password, as well as old security question and answer.
	 * @return 				the updated user account.
	 */
	@PostMapping(value = "/account/{username}/resetPassword")
	@ResponseBody
	public UserAccountResponseDto changePassword(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@PathVariable("username") String username,
			@Valid @RequestBody ResetPasswordRequestDto request) {
		AccessTokenHelper.parseAccessToken(token, false, null);
		var user = userService.getUserAccount(username);
		user = userService.updatePassword(user, user.getSecurityQuestion(),
				request.securityAnswer, request.password);
		return new UserAccountResponseDto(user);
	}
	
	/**
	 * Change password of a given account.
	 * 
	 * @HTTPmethod			PUT
	 * @URL					/account/resetPassword
	 * @param token			Bearer access token. No required registration or user claims.
	 * @param request		DTO holding new password, as well as old security question and answer.
	 * @return 				the updated user account.
	 */
	@PutMapping(value = "/account/resetPassword")
	@ResponseBody
	public UserAccountResponseDto changePassword(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody ResetPasswordRequestDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true, null);
		var user = userService.getUserAccount(credentials.userAccountId);
		user = userService.updatePassword(user, user.getSecurityQuestion(),
				request.securityAnswer, request.password);
		return new UserAccountResponseDto(user);
	}
	
	/**
	 * Change security question and answer for an existing account.
	 * 
	 * @HTTPmethod			PUT
	 * @URL					/account/security
	 * @param token			Bearer access token. Must be registered.
	 * @param request		The user's password, as well as new question and answer.
	 * @return				The updated user account.
	 */
	@PutMapping(value="/account/security")
	@ResponseBody
	public UserAccountResponseDto changeSecurityQnA(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody ResetSecurityQuestionAnswerRequestDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true, null);
		var user = userService.getUserAccount(credentials.userAccountId);
		var response = userService.updateSecurityQandA(user, request.password,
				request.securityQuestion, request.securityAnswer);

		return new UserAccountResponseDto(response);
	}

}
