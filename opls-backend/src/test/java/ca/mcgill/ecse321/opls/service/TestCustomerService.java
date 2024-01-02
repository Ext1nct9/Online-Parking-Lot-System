package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import ca.mcgill.ecse321.opls.model.Customer;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.CustomerRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;

/**
 * Test the CustomerService class.
 */
@ExtendWith(MockitoExtension.class)
public class TestCustomerService {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private UserAccountClaimRepository claimRepository;

	@InjectMocks
	private CustomerService service;

	private UserAccount userAccount, newAcc;
	private Customer customer;


	private void assertEntityEquals(Customer expected, Customer actual) {
		assertNotNull(actual);
		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getUuid(), actual.getUuid());
		assertEquals(expected.getSavedLicensePlate(), actual.getSavedLicensePlate());
	}

	@BeforeEach
	public void setupMocks() {
		userAccount = new UserAccount();
		userAccount.overrideId(15);
		customer = new Customer();
		customer.setUserAccount(userAccount);
		customer.overrideId(155);
		customer.setSavedLicensePlate("asdf");
		
        Customer[] customers = new Customer[1];
        customers[0] = customer;


		lenient()
		.when(customerRepository.findCustomerByUuid(customer.getUuid()))
		.thenAnswer((InvocationOnMock invocation) -> customer);
		lenient()
		.when(customerRepository.findCustomerById(customer.getId()))
		.thenAnswer((InvocationOnMock invocation) -> customer);
		lenient()
		.when(customerRepository.findCustomerByUserAccountId(userAccount.getId()))
		.thenAnswer((InvocationOnMock invocation) -> customer);
		lenient()
		.when(customerRepository.findCustomerByUserAccount(userAccount))
		.thenAnswer((InvocationOnMock invocation) -> customer);
        lenient().when(customerRepository.save(any())).
        thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));
        
	}

	/**
	 * Test getting customers.
	 */
	@Test
	public void testGetCustomer() {
		assertEntityEquals(customer, service.getCustomer(userAccount));
		assertEntityEquals(customer, service.getCustomer(customer.getUuid()));
		assertEntityEquals(customer, service.getCustomerByCustomerId(customer.getId()));
		assertEntityEquals(customer, service.getCustomerByUAId(userAccount.getId()));

		assertThrowsApiException(HttpStatus.NOT_FOUND, "Customer not found.",
				() -> service.getCustomer(new UserAccount()));
		assertThrowsApiException(HttpStatus.NOT_FOUND, "Customer not found.",
				() -> service.getCustomer(UUID.randomUUID()));
		assertThrowsApiException(HttpStatus.NOT_FOUND, "Customer not found.",
				() -> service.getCustomerByCustomerId(-5));
		assertThrowsApiException(HttpStatus.NOT_FOUND, "Customer not found.",
				() -> service.getCustomerByUAId(-5));
	}

	/**
	 * Test creating customers.
	 */
	@Test
	public void testCreateCustomer() {
        // test valid request
		newAcc = new UserAccount();
        Customer c1 = service.createCustomer(newAcc, "asdf", "5" );
        assertNotNull(c1);
        assertEquals(newAcc, c1.getUserAccount());
        assertEquals("asdf", c1.getSavedLicensePlate());
        assertEquals("5", c1.getSavedBillingAccountId());
        
        //test invalid request (user account already associated with a customer)
		assertThrowsApiException(HttpStatus.CONFLICT,
				"A customer is already associated with this user account.",
				() -> service.createCustomer(userAccount, "lkjh", "yourballs"));
		
		//test invalid request (user account is null
		assertThrowsApiException(HttpStatus.BAD_REQUEST,
				"invalid_request",
				() -> service.createCustomer(null, "lkjh", "yourballs"));
	}
	
	/**
	 * Test updating customer
	 */
	@Test 
	public void testUpdateCustomer() {
		Customer c1 = service.updateCustomer(customer, "IP00PH4RD", "cash");
		assertNotNull(c1);
		assertEquals("cash", c1.getSavedBillingAccountId());
		assertEquals("IP00PH4RD", c1.getSavedLicensePlate());
	}
	
	/**
	 * Test setting customer attributes.
	 */
	@Test 
	public void testSetCustomer() {
		Customer c1 = service.setCustomerBilling(customer.getUuid(), "Jeff Benzos bank account");
		assertNotNull(c1);
		assertEquals("Jeff Benzos bank account", c1.getSavedBillingAccountId());
		
		Customer c2 = service.setCustomerLicense(customer.getUuid(), "RYANGOSLINGDRIVE");
		assertNotNull(c2);
		assertEquals("RYANGOSLINGDRIVE", c2.getSavedLicensePlate());
		
		newAcc = new UserAccount();
		Customer c3 = service.setCustomerUserAccount(newAcc, customer.getUuid());
		assertNotNull(c3);
		assertEquals(newAcc, c3.getUserAccount());
	}
	
	/**
	 * Test deleting customer.
	 */
	@Test 
	public void testDeleteCustomer() {
		//valid deletion
		var cust = customerRepository.findCustomerById(customer.getId());
		//executing the method
		var res = service.deleteCustomer(customer);
		assertEquals(cust.getUuid(),res.getUuid());
		assertEquals(cust.getSavedBillingAccountId(), res.getSavedBillingAccountId());
		assertEquals(cust.getSavedLicensePlate(),res.getSavedLicensePlate());

	}
	
	
}
