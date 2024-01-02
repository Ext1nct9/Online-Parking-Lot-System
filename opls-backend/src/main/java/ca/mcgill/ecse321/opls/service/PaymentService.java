package ca.mcgill.ecse321.opls.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.exception.OplsApiException;

/**
 * Mock service to represent payments.
 */
@Service
public class PaymentService {

	/** Account number to indicate cash payment. */
	public static final String CASH_ACCOUNT = "0000000000000000";

	/**
	 * Submit a payment to the bank.
	 * 
	 * @param accountNumber
	 *            The credit card or saved account number.
	 * @param amount
	 *            The amount to charge.
	 */
	public double submitPayment(boolean isEmployee, String accountNumber,
			double amount) {
		if (isEmployee) {
			if (accountNumber.equals(CASH_ACCOUNT)) {
				System.out.printf("Charge $%.2f in cash.\n", amount);
				return amount;
			}
		}

		if (accountNumber.length() == 16) {
			System.out.printf("Charge $%.2f to account.\n", amount, accountNumber);
			return amount;
		}
		else {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "Rejected payment.");
		}
	}

}
