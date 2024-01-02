package ca.mcgill.ecse321.opls.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.opls.auth.AccessTokenHelper;
import ca.mcgill.ecse321.opls.dto.CustomerDto;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.service.CustomerService;
import ca.mcgill.ecse321.opls.service.UserAccountService;
import jakarta.validation.Valid;

/**
 * Customer profile operations.
 */
@RestController
@CrossOrigin
public class CustomerController {

	@Autowired
	CustomerService customerService;

	@Autowired
	UserAccountService userAccountService;

	/**
	 * Retrieves customer of the current session.
	 * 
	 * @HTTPmethod 			GET
	 * @URL					/customer
	 * @param token			Bearer access token. Must have the CUSTOMER user claim.
	 * @return				The customer entity.
	 */
	@GetMapping(value = "/customer")
	@ResponseBody
	public CustomerDto getCustomer(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true,
				Collections.singleton(OAuthClaim.CUSTOMER));
		return new CustomerDto(customerService
				.getCustomerByUAId(credentials.getCustomerUserId()));
	}

	/**
	 * Updates customer of the current session.
	 * 
	 * @HTTPmethod			PATCH
	 * @URL 				/customer
	 * @param token			Bearer access token. Must have the CUSTOMER user claim.
	 * @param request		The fields to update.
	 * @return				The updated customer profile.
	 */
	@PatchMapping(value = "/customer")
	@ResponseBody
	public CustomerDto updateCustomer(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody CustomerDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true,
				Collections.singleton(OAuthClaim.CUSTOMER));
		
		var customer = customerService
				.getCustomerByUAId(credentials.getCustomerUserId());
		return new CustomerDto(customerService.updateCustomer(customer,
				request.savedLicensePlate, request.billingAccountId));
	}

	/**
	 * Creates customer with given information for the current user.
	 * 
	 * @HTTPmethod			POST
	 * @URL 				/customer
	 * @param token			Bearer access token. Must be registered.
	 * @param request		The customer profile to create.
	 * @return				The created profile.
	 */
	@PostMapping(value = "/customer")
	@ResponseBody
	public CustomerDto createCustomer(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
			@Valid @RequestBody CustomerDto request) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true, null);
		
		return new CustomerDto(customerService.createCustomer(
				userAccountService.getUserAccount(credentials.userAccountId),
				request.savedLicensePlate, request.billingAccountId));
	}
	


	/**
	 * Delete the customer profile for the current session.
	 * 
	 * @HTTPMethod			DELETE
	 * @URL					/customer
	 * @param token			Bearer access token. Must have the CUSTOMER user claim.
	 * @return				The deleted entity.
	 */
	@DeleteMapping(value = "/customer")
	@ResponseBody
	public CustomerDto deleteCustomer(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token) {
		var credentials = AccessTokenHelper.parseAccessToken(token, true,
				Collections.singleton(OAuthClaim.CUSTOMER));

		var customer = customerService
				.getCustomerByUAId(credentials.userAccountId);
		customerService.deleteCustomer(customer);
		return new CustomerDto(customer);
	}

}
