package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

@SpringBootTest
public class TestEmployeeRepository {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private ParkingSpotRepository parkingSpotRepository;

	@Autowired
	private ParkingSpotBookingRepository parkingSpotBookingRepository;

	@Autowired
	private VehicleServiceRepository vehicleServiceRepository;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@AfterEach
	public void clearDatabase() {
		employeeRepository.deleteAll();
		userAccountRepository.deleteAll();
		parkingSpotBookingRepository.deleteAll();
		parkingSpotRepository.deleteAll();
		vehicleServiceRepository.deleteAll();
	}
	// Method to create new Employee and linked User.
	private Employee newEmployeeAndUser(String title, Double salary,
			String username, String firstName, String lastName) {
		var userAccount = userAccountRepository
				.save(TestUserAccountRepository.newAccount(username, firstName,
						lastName, "password", "null", "null"));
		var employee = new Employee();
		employee.setJobTitle(title);
		employee.setSalary(salary);
		employee.setUserAccount(userAccount);
		employee = employeeRepository.save(employee);

		return employee;
	}

	/**
	 * Test the creation of an employee with an associated user account
	 */
	@Test
	public void testCreation() {
		// create user account and employee entries
		String title = "janitor";
		Double salary = 69420.00;
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var employee = newEmployeeAndUser(title, salary, username, firstName,
				lastName);
		int id = employee.getId();

		// fetch employee
		var result = employeeRepository.findEmployeeById(id);
		assertNotNull(result);
		assertEquals(title, result.getJobTitle());
		assertEquals(salary, result.getSalary());

		// validate linked user
		UserAccount resultUserAccount = result.getUserAccount();
		assertNotNull(resultUserAccount);
		assertEquals(username, resultUserAccount.getUsername());
		assertEquals(firstName, resultUserAccount.getFirstName());
		assertEquals(lastName, resultUserAccount.getLastName());
	}

	/**
	 * Test the changes to the fields of an employee with an associated user
	 * account
	 */
	@Test
	public void testUpdate() {
		// create user account and employee entries
		String title = "professional eater";
		Double salary = 60.00;
		var username = "zeyuanfu123";
		var firstName = "fu";
		var lastName = "yuan ze";
		var employee = newEmployeeAndUser(title, salary, username, firstName,
				lastName);
		int id = employee.getId();

		// fetch employee
		var result = employeeRepository.findEmployeeById(id);
		assertNotNull(result);
		assertEquals(title, result.getJobTitle());
		assertEquals(salary, result.getSalary());

		// new values
		Double newSalary = 70000.00;
		String newTitle = "brain damager";
		// update employee
		employee.setSalary(newSalary);
		employee.setJobTitle(newTitle);

		employee = employeeRepository.save(employee);
		var result2 = employeeRepository.findEmployeeById(id);
		assertEquals(newTitle, result2.getJobTitle());
		assertEquals(newSalary, result2.getSalary());

		var employees = employeeRepository.getEmployees();
		int i = 0;
		for (var e : employees) {
			++i;
			assertEquals(newTitle, e.getJobTitle());
			assertEquals(newSalary, e.getSalary());
		}
		assertEquals(1, i);
	}
	/**
	 * Test the deletion of an employee with an associated user account
	 */
	@Test
	public void testDelete() {
		// create user account and employee entries
		String title = "professional eater";
		Double salary = 60.00;
		var username = "zeyuanfu123";
		var firstName = "fu";
		var lastName = "yuan ze";
		var employee = newEmployeeAndUser(title, salary, username, firstName,
				lastName);
		int id = employee.getId();

		// fetch employee
		var result = employeeRepository.findEmployeeById(id);
		assertNotNull(result);
		assertEquals(title, result.getJobTitle());
		assertEquals(salary, result.getSalary());
		UserAccount resultUserAccount = result.getUserAccount();
		int userAccountid = resultUserAccount.getId();
		// delete employee
		employeeRepository.delete(result);
		assertNull(employeeRepository.findEmployeeById(id));
		// deleting an employee does not delete the user account.
		assertNotNull(
				userAccountRepository.findUserAccountById(userAccountid));;
	}

}
