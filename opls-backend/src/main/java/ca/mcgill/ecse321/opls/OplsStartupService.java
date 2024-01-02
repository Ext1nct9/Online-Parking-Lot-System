package ca.mcgill.ecse321.opls;

import static ca.mcgill.ecse321.opls.auth.OAuthHelper.OAUTH_ACCESS_TOKEN_EXPIRY;

import java.sql.Time;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.dto.auth.AccessToken;
import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.OAuthClient;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.CustomerRepository;
import ca.mcgill.ecse321.opls.repository.EmployeeRepository;
import ca.mcgill.ecse321.opls.repository.EmployeeScheduleRepository;
import ca.mcgill.ecse321.opls.repository.OAuthClientRepository;
import ca.mcgill.ecse321.opls.repository.OAuthClientSessionRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemScheduleRepository;
import ca.mcgill.ecse321.opls.repository.ParkingSpotBookingRepository;
import ca.mcgill.ecse321.opls.repository.ParkingSpotRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import ca.mcgill.ecse321.opls.repository.VehicleServiceBookingRepository;
import ca.mcgill.ecse321.opls.repository.VehicleServiceRepository;

@Service
public class OplsStartupService {

	private static final String ADMIN_DEFAULT_USERNAME = "admin";
	private static final String ADMIN_DEFAULT_PW = "pw123";
	private static final String ADMIN_DEFAULT_SEC_Q = "What is the code?";
	private static final String ADMIN_DEFAULT_SEC_ANS = "12345";
	private static int createdAdminId = -1;

	private static final String WEBSITE_CLIENT_ID = "website";
	private static final String WEBSITE_CLIENT_SECRET = "secret";

	private final Double monthlyFee = 60.00, incrementFee = 0.25;

	private final int incrementTime = 15, maxIncrementTime = 12 * 60;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmployeeScheduleRepository employeeScheduleRepository;

	@Autowired
	private OAuthClientRepository clientRepository;

	@Autowired
	private OAuthClientSessionRepository clientSessionRepository;

	@Autowired
	private ParkingLotSystemRepository parkingLotSystemRepository;

	@Autowired
	private ParkingLotSystemScheduleRepository parkingLotSystemScheduleRepository;

	@Autowired
	private ParkingSpotBookingRepository parkingSpotBookingRepository;

	@Autowired
	private ParkingSpotRepository parkingSpotRepository;

	@Autowired
	private UserAccountClaimRepository userAccountClaimRepository;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private VehicleServiceBookingRepository vehicleServiceBookingRepository;

	@Autowired
	private VehicleServiceRepository vehicleServiceRepository;

	/** Save a default system configuration. */
	public void initializeConfiguration() {
		var config = parkingLotSystemRepository.getActiveParkingLotSystem();
		if (config == null) {
			config = new ParkingLotSystem();
			config.setDisplayName("Default");
			config.setIncrementTime(incrementTime);
			config.setMaxIncrementTime(maxIncrementTime);
			config.setIncrementFee(incrementFee);
			config.setMonthlyFee(monthlyFee);
			config.activate();
			config = parkingLotSystemRepository.save(config);
		}

		if (!parkingLotSystemScheduleRepository.findActiveParkingLotSchedules()
				.iterator().hasNext()) {
			for (var day : Day.values()) {
				if (parkingLotSystemScheduleRepository
						.findActiveParkingLotScheduleByDay(day) == null) {
					parkingLotSystemScheduleRepository.save(
							config.addSchedule(day, Time.valueOf("00:00:00"),
									Time.valueOf("23:59:59")));
				}
			}
		}
	}

	private static String getOrDefault(String str, String defaultStr) {
		return str == null ? defaultStr : str;
	}

	/**
	 * Generate a default user.
	 * 
	 * @param username
	 *            The username, defaults to ADMIN_DEFAULT_USERNAME.
	 * @param pw
	 *            The password, defaults to ADMIN_DEFAULT_PASSWORD.
	 * @param secQ
	 *            The security question, defaults to ADMIN_DEFAULT_SEC_Q.
	 * @param secAns
	 *            The security answer, defaults to ADMIN_DEFAULT_SEC_ANS.
	 * @param firstName
	 *            The first name of the user.
	 * @param lastName
	 *            The last name of the user.
	 * @return The user account object.
	 * @throws Exception
	 *             If setting the password fails.
	 */
	public static UserAccount defaultUser(String username, String pw,
			String secQ, String secAns, String firstName, String lastName)
			throws Exception {
		var userAccount = new UserAccount();
		userAccount.setFirstName(firstName);
		userAccount.setLastName(lastName);
		userAccount.setUsername(getOrDefault(username, ADMIN_DEFAULT_USERNAME));
		userAccount.setPassword(getOrDefault(pw, ADMIN_DEFAULT_PW));
		userAccount.setSecurityAnswer(getOrDefault(secQ, ADMIN_DEFAULT_SEC_Q),
				getOrDefault(secAns, ADMIN_DEFAULT_SEC_ANS));
		return userAccount;
	}

	/** Initialize all parking spots. */
	public void initializeParkingLotStructure() {
		if (parkingSpotRepository.count() == 0) {
			char floor;
			int i;

			// floor A has specific configuration
			// A00-A04 are reserved for employees
			floor = 'A';
			for (i = 0; i < 5; ++i) {
				parkingSpotRepository.save(new ParkingSpot(floor, i,
						VehicleType.REGULAR, ParkingSpotStatus.CLOSED));
			}
			// A05-A24 are open spots for large vehicles
			for (; i < 25; ++i) {
				parkingSpotRepository.save(new ParkingSpot(floor, i,
						VehicleType.LARGE, ParkingSpotStatus.OPEN));
			}
			// A25-A69 are open spots for regular vehicles
			for (; i < 70; ++i) {
				parkingSpotRepository.save(new ParkingSpot(floor, i,
						VehicleType.REGULAR, ParkingSpotStatus.OPEN));
			}

			// floors B and C are reserved and have 100 spots
			for (floor = 'B'; floor <= 'C'; ++floor) {
				for (i = 0; i < 100; ++i) {
					parkingSpotRepository.save(new ParkingSpot(floor, i,
							VehicleType.REGULAR, ParkingSpotStatus.RESERVED));
				}
			}

			// floors D and E are open and have 100 spots
			for (floor = 'D'; floor <= 'E'; ++floor) {
				for (i = 0; i < 100; ++i) {
					parkingSpotRepository.save(new ParkingSpot(floor, i,
							VehicleType.REGULAR, ParkingSpotStatus.OPEN));
				}
			}
		}
	}

	/** Initialize the website OAuthClient and admin UserAccount. */
	private void initializeOAuth(String username, String pw, String secQ,
			String secAns) throws Exception {
		// create website client
		if (clientRepository
				.findOAuthClientByClientId(WEBSITE_CLIENT_ID) == null) {
			clientRepository.save(new OAuthClient("Website", WEBSITE_CLIENT_ID,
					WEBSITE_CLIENT_SECRET));
		}

		// create admin user
		var adminUser = userAccountRepository
				.findUserAccountByUsername("admin");
		if (adminUser == null) {
			var userAccount = defaultUser(username, pw, secQ, secAns, "First",
					"Last");
			userAccountRepository.save(userAccount);
			createdAdminId = userAccount.getId();

			// add claims
			userAccountClaimRepository
					.save(userAccount.addUserAccountClaim(OAuthClaim.ADMIN));
			userAccountClaimRepository
					.save(userAccount.addUserAccountClaim(OAuthClaim.EMPLOYEE));
			userAccountClaimRepository
					.save(userAccount.addUserAccountClaim(OAuthClaim.CUSTOMER));
		} else {
			createdAdminId = adminUser.getId();
		}
	}

	/** Initialize database data for boot. */
	public void startupBoot() throws Exception {
		initializeConfiguration();
		initializeParkingLotStructure();

		String defaultPw = System.getenv("opls_admin_default_pw");
		String defaultSecQ = System.getenv("opls_admin_default_security_q");
		String defaultSecAns = System.getenv("opls_admin_default_security_ans");
		initializeOAuth(ADMIN_DEFAULT_USERNAME, defaultPw, defaultSecQ,
				defaultSecAns);
	}

	/** Clear existing database data for integration testing. */
	public void startupTest() throws Exception {
		vehicleServiceBookingRepository.deleteAll();
		vehicleServiceRepository.deleteAll();

		parkingSpotBookingRepository.deleteAll();
		parkingSpotRepository.deleteAll();

		customerRepository.deleteAll();

		employeeScheduleRepository.deleteAll();
		employeeRepository.deleteAll();

		parkingLotSystemScheduleRepository.deleteAll();
		parkingLotSystemRepository.deleteAll();

		clientSessionRepository.deleteAll();
		clientRepository.deleteAll();

		userAccountClaimRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	/** Populate an access token with a default user. */
	public static AccessToken createDefaultAccessToken() {
		AccessToken defaultToken = new AccessToken();
		defaultToken.expiresOn = new Date().getTime()
				+ OAUTH_ACCESS_TOKEN_EXPIRY;
		defaultToken.oauthClaims.add(OAuthClaim.ADMIN);
		defaultToken.oauthClaims.add(OAuthClaim.EMPLOYEE);
		defaultToken.oauthClaims.add(OAuthClaim.CUSTOMER);
		defaultToken.isRegistered = true;
		defaultToken.oauthClientId = WEBSITE_CLIENT_ID;
		defaultToken.userAccountId = createdAdminId;
		return defaultToken;
	}

	public Double getMonthlyFee() {
		return monthlyFee;
	}

	public Double getIncrementFee() {
		return incrementFee;
	}

	public int getIncrementTime() {
		return incrementTime;
	}

	public int getMaxIncrementTime() {
		return maxIncrementTime;
	}
}
