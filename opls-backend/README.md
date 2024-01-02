## Development and running

### Running JUnit tests
In order to run the project (i.e. `./gradlew clean build`), you must have PostgreSQL setup on your machine. The default user that the application logs in with is `postgres`, with a password of `postgres`. If your user or password is different, please set the environment variables `POSTGRES_USER` and `POSTGRES_PASSWORD` with your respective values. Finally, you must create a database in Postgres named `opls`, and ensure that your user has superuser access to it.

### Running the application locally
You must have the same environment variables set for Postgres as stated above. To run the application so it is accessible on your computer as a website, run the command `./gradlew bootRun`.

To have a default user so that you can call endpoints during development without having to go through the authorization flow, set the environment variable, `opls_default_auth` to true.

### Local development of endpoints

To make your controller endpoints accept authorization, you must have the authorization header as a method parameter. For example:
```java
@RequestMapping(method = RequestMethod.GET, value = "/profile")
public ProfileResponse getProfile(
        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    var token = AccessTokenHelper.parseAccessToken(authorization, true, null);
    
    ...
}
```

* `required`: whether your endpoint must accept an authorization header. Set this to `false` because the helper method will throw specific errors when the header is null.
* 

### Local development of integration tests
Another consequence of the authorization flow is that we must authorize our tester client. We created a base class, `OplsApiTester` that automatically does this for us. Please extend it and make the API calls as follows:

```java
@SpringBootTest
public class IntegrationTester extends OplsApiTester {
    ...

    /** Specify the claims the tester account should have. */
    @Override
	@PostConstruct
	public void specifyAuthentication() {
		this.addDefaultClaim(OAuthClaim.ADMIN);
		this.addDefaultClaim(OAuthClaim.CUSTOMER);
		this.addDefaultClaim(OAuthClaim.EMPLOYEE);
	}

    ...

    @Test
	public void testGetProfile() {
		// get user account the tester has logged in with
		var user = this.getUserAccount();

        // get the tester OAuthClient
        var tester = this.getTesterClient();

        // make the API request
		var resp = this.exchange(HttpMethod.GET, "/profile",
				ProfileResponseDto.class, HttpStatus.OK).getBody();

        // make an API request with a request body
        resp = this.exchange(HttpMethod.PUT, "/profile", body, 
                ProfileRequestDto.class, ProfileResponseDto.class, HttpStatus.OK).getBody();
    }

    ...
}
```

* `specifyAuthentication`: this method adds default claims that the tester client should have. Tailor this to your tests based on the endpoints.