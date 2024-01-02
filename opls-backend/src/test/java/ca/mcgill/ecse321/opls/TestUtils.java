package ca.mcgill.ecse321.opls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatusCode;

import ca.mcgill.ecse321.opls.dto.OplsApiErrorResponseDto;
import ca.mcgill.ecse321.opls.exception.OplsApiException;

public class TestUtils {

	/** Assert the Executable throws a specific OplsApiException. */
	public static void assertThrowsApiException(HttpStatusCode expectedCode,
			String expectedReason, Executable executable) {
		assertThrowsApiException(expectedCode, expectedReason, null,
				executable);
	}

	/** Assert the Executable throws a specific OplsApiException. */
	public static void assertThrowsApiException(HttpStatusCode expectedCode,
			String expectedReason, String expectedDetails,
			Executable executable) {
		var ex = assertThrows(OplsApiException.class, executable);
		assertEquals(expectedCode, ex.getStatusCode());
		assertEquals(expectedReason, ex.getReason());

		if (expectedDetails != null) {
			assertEquals(expectedDetails, ex.getDetailMessageCode());
		}
	}

	/** Ensure returned error object is correct. */
	public static void validateErrorObj(String expectedError,
			String expectedMessage, OplsApiErrorResponseDto error) {
		assertNotNull(error);
		if (expectedError != null) {
			assertEquals(expectedError, error.error);
		}
		if (expectedMessage != null) {
			assertEquals(expectedMessage, error.message);
		}
	}

}
