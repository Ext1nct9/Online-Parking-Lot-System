package ca.mcgill.ecse321.opls;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.exception.OplsApiException;

/**
 * Helper methods to deal with dates.
 */
public class DateHelper {

	public static final String TIME_FORMAT = "HH:mm:ss";

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final SimpleDateFormat parser = new SimpleDateFormat(
			DATE_FORMAT);

	/**
	 * Parse date from the format: DateHelper.DATE_FORMAT. For example, parses
	 * 2023-02-16 23:12:33.
	 */
	public static Date parseDate(String date) {
		try {
			return parser.parse(date);
		} catch (ParseException e) {
			throw new OplsApiException(HttpStatus.BAD_REQUEST, "Invalid date.");
		}
	}

}
