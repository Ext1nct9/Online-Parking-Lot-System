package ca.mcgill.ecse321.opls.repository;

/** Stored queries to be used across multiple tables. */
public class QueryHelper {
	/** Independent where clauses. */
	public static class WhereClause {
		/**
		 * Query entries with a range containing the current date. Table must
		 * have "start_date" and "end_date" columns.
		 */
		public static final String CURRENT_ACTIVE = "(start_date <= NOW() AND end_date >= NOW())";
	}
}
