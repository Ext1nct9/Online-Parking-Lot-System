package ca.mcgill.ecse321.opls.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.Employee.EmployeeSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;

@SpringBootTest
public class TestEmployeeScheduleRepository {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmployeeScheduleRepository employeeScheduleRepository;

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
		employeeScheduleRepository.deleteAll();
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
	 * Test the creation of an employee with associated schedules
	 */
	@Test
	public void testCreationWithSchedule() {
		// create employee and user
		String title = "janitor";
		Double salary = 69420.00;
		var username = "spongebob_squarepants_123";
		var firstName = "lightning";
		var lastName = "mcqueen";
		var employee = newEmployeeAndUser(title, salary, username, firstName,
				lastName);
		int id = employee.getId();

		// create employee schedules
		EmployeeSchedule es1 = new EmployeeSchedule();
		EmployeeSchedule es2 = new EmployeeSchedule();
		EmployeeSchedule es3 = new EmployeeSchedule();
		HashSet<EmployeeSchedule> schedules = new HashSet<>();
		schedules.add(es1);
		schedules.add(es2);
		schedules.add(es3);
		es1.setDay(Day.MONDAY);
		es2.setDay(Day.TUESDAY);
		es3.setDay(Day.WEDNESDAY);
		HashSet<Day> days = new HashSet<>();
		days.add(Day.MONDAY);
		days.add(Day.TUESDAY);
		days.add(Day.WEDNESDAY);
		Time now = Time.valueOf("10:10:10");
		Time after = Time.valueOf("20:20:20");
		HashSet<Integer> scheduleIds = new HashSet<>();
		for (EmployeeSchedule es : schedules) {
			es.setEmployee(employee);
			es.setStartTime(now);
			es.setEndTime(after);
			es = employeeScheduleRepository.save(es);
			scheduleIds.add(es.getId());
		}

		// validate employee
		var result = employeeRepository.findEmployeeById(id);
		assertNotNull(result);
		assertEquals("janitor", result.getJobTitle());

		// validate employee schedules
		Set<EmployeeSchedule> schedulesList = (Set<EmployeeSchedule>) result
				.getSchedules();
		assertEquals(3, schedulesList.size());
		for (EmployeeSchedule employeeSchedule : schedulesList) {
			assertTrue(scheduleIds.contains(employeeSchedule.getId()));
			scheduleIds.remove(employeeSchedule.getId());
			assertTrue(days.contains(employeeSchedule.getDay()));
			days.remove(employeeSchedule.getDay());
			assertEquals(now, employeeSchedule.getStartTime());
		}
		assertEquals(0, scheduleIds.size());
		assertEquals(0, days.size());
	}

}
