package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Test the PaymentService class.
 */
@ExtendWith(MockitoExtension.class)
public class TestPaymentService {

	@InjectMocks
	private PaymentService service;

	@Test
	public void testSubmitPayment() {
		final double amount = 15.0;

		// submit valid payment
		assertEquals(amount,
				service.submitPayment(false, "1234123412341234", amount));
		assertEquals(amount, service.submitPayment(true,
				PaymentService.CASH_ACCOUNT, amount));

		// submit invalid payment
		assertThrowsApiException(HttpStatus.BAD_REQUEST, "Rejected payment.",
				() -> service.submitPayment(false, "1234", amount));
	}

}
