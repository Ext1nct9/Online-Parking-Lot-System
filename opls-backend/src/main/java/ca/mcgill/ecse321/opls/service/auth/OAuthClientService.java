package ca.mcgill.ecse321.opls.service.auth;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.auth.OAuthHelper;
import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.repository.OAuthClientRepository;
import jakarta.transaction.Transactional;

/**
 * Service to handle OAuthClient objects.
 */
@Service
public class OAuthClientService {

	@Autowired
	private OAuthClientRepository clientRepo;

	/**
	 * Get an OAuthClient object from the database.
	 * 
	 * @param authorizationHeader
	 *            The base64 encoded authorization header. Must be in the form
	 *            "Basic <base64(clientId:secret)>".
	 * @return The OAuthClient object if it exists.
	 * @throws OplsApiException
	 *             if the client does not exist.
	 */
	@Transactional
	public OAuthClient getOAuthClientFromAuthorizationHeader(
			String authorizationHeader) {
		String clientId = null;
		String clientSecret = null;
		try {
			// decode the header
			var tokens = authorizationHeader.split(" ");
			if (!tokens[0].equalsIgnoreCase("basic")) {
				throw new Exception();
			}

			var clientIdSecret = new String(
					Base64.getUrlDecoder().decode(tokens[1])).split(":");

			clientId = clientIdSecret[0];
			clientSecret = clientIdSecret[1];
		} catch (Exception e) {
			throw OAuthHelper.Errors
					.invalidRequest("Malformed Authorization header.");
		}

		OAuthClient client = clientRepo
				.findOAuthClientByClientIdAndSecret(clientId, clientSecret);
		if (client == null) {
			throw OAuthHelper.Errors.invalidClient("Client not found.");
		}

		return client;
	}

}
