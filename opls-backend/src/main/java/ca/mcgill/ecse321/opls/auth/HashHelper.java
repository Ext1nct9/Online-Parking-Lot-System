package ca.mcgill.ecse321.opls.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

/**
 * Class to help with hashing operations. Performs hashing using SHA-512 and
 * randomly generates salts of length 32 bytes. Returns all strings encoded as
 * base64url strings.
 */
public class HashHelper {
	/** Hashing algorithm to use. */
	private static final String HASH_ALGORITHM = "SHA-512";

	/** Number of bytes to use in the hash salt. */
	private static final int HASH_SALT_LEN = 32;

	/** Length of output strings encoded to base64url. */
	public static final int HASH_LEN_ENCODED = 88;
	public static final int HASH_SALT_LEN_ENCODED = 44;

	/**
	 * Model for the result of a hashing operation.
	 */
	public static class HashResult {
		private String hashedData;
		private String salt;

		public HashResult(String hashedData, String salt) {
			this.hashedData = hashedData;
			this.salt = salt;
		}

		/**
		 * Get the hash result as a base64url encoded string.
		 */
		public String getHashedData() {
			return hashedData;
		}

		/**
		 * Get the randomly generated salt used to hash the data as a base64url
		 * encoded string.
		 */
		public String getSalt() {
			return salt;
		}
	}
	
	/**
	 * Randomly generate a salt and hash it with the sensitive data.
	 * 
	 * @param sensitiveData
	 *            The data to hash.
	 * @param saltBytes
	 * 			  The bytes to salt the data with.           
	 * @return The hashed data and the generated salt bytes, encoded as a
	 *         base64url string.
	 */
	public static HashResult hashData(String sensitiveData, byte[] saltBytes) {
		// get data bytes
		byte[] dataBytes = sensitiveData.getBytes(StandardCharsets.UTF_8);

		// hash
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		digest.update(dataBytes);
		digest.update(saltBytes);
		byte[] hashedBytes = digest.digest();

		// return hash and salt as base64url encoded string
		var b64e = Base64.getUrlEncoder();
		return new HashResult(b64e.encodeToString(hashedBytes),
				b64e.encodeToString(saltBytes));
	}

	/**
	 * Randomly generate a salt and hash it with the sensitive data.
	 * 
	 * @param sensitiveData
	 *            The data to hash.
	 * @return The hashed data and the generated salt bytes, encoded as a
	 *         base64url string.
	 */
	public static HashResult hashData(String sensitiveData) {
		// get salt bytes
		byte[] saltBytes = new byte[HASH_SALT_LEN];
		Random rnd = new Random();
		rnd.nextBytes(saltBytes);
		
		return hashData(sensitiveData, saltBytes);
	}

	/**
	 * Hash the input data and test if it matches the expected value.
	 * 
	 * @param testData
	 *            The input data.
	 * @param encodedSalt
	 *            The original salt encoded as a base64url string.
	 * @param storedHash
	 *            The stored hash to compare the result against.
	 * @return Whether the hashed data matches the stored hash.
	 */
	public static boolean testHash(String testData, String encodedSalt,
			String storedHash) {
		// get data bytes
		byte[] dataBytes = testData.getBytes(StandardCharsets.UTF_8);

		// get salt bytes
		var b64d = Base64.getUrlDecoder();
		byte[] saltBytes = b64d.decode(encodedSalt);

		// hash
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
		digest.update(dataBytes);
		digest.update(saltBytes);
		byte[] hashedBytes = digest.digest();

		// compare to stored
		var b64e = Base64.getUrlEncoder();
		return b64e.encodeToString(hashedBytes).equals(storedHash);
	}
}
