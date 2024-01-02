package ca.mcgill.ecse321.opls.exception;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import ca.mcgill.ecse321.opls.dto.OplsApiErrorResponseDto;

/**
 * Methods to catch API exceptions and intercept before sending a response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handle all exceptions of type OplsApiException.
	 */
	@ExceptionHandler(OplsApiException.class)
	@ResponseBody
	public ResponseEntity<OplsApiErrorResponseDto> handleResponseStatusException(
			OplsApiException ex) {
		return new ResponseEntity<OplsApiErrorResponseDto>(
				new OplsApiErrorResponseDto(ex.getReason(),
						ex.getDetailMessageCode()),
				ex.getStatusCode());
	}

	/**
	 * Handle invalid conversions.
	 */
	@ExceptionHandler(ConversionFailedException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<OplsApiErrorResponseDto> handleConflict(
			ConversionFailedException ex) {
		return new ResponseEntity<OplsApiErrorResponseDto>(
				new OplsApiErrorResponseDto("Invalid conversion.",
						ex.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle JSON parsing exception.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<OplsApiErrorResponseDto> handleRequestException(
			HttpMessageNotReadableException ex) {
		var response = new OplsApiErrorResponseDto("Invalid request body.");
		response.message = ex.getMessage();
		return new ResponseEntity<OplsApiErrorResponseDto>(response,
				HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle model validation exceptions.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<OplsApiErrorResponseDto> handleValidationException(
			MethodArgumentNotValidException ex) {
		var response = new OplsApiErrorResponseDto("Invalid request body.");
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			response.addField(fe.getField(), fe.getDefaultMessage());
		}
		return new ResponseEntity<OplsApiErrorResponseDto>(response,
				ex.getStatusCode());
	}

	/**
	 * Handle route not found.
	 */
	@ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<OplsApiErrorResponseDto> handleNotFoundException(
			Exception ex) {
		return new ResponseEntity<OplsApiErrorResponseDto>(
				new OplsApiErrorResponseDto("Route not found."),
				HttpStatus.NOT_FOUND);
	}

	/**
	 * Handle all other exceptions.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<OplsApiErrorResponseDto> handleGeneralException(
			Exception ex) {
		return new ResponseEntity<OplsApiErrorResponseDto>(
				new OplsApiErrorResponseDto("Something went wrong.",
						ex.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
