package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.Employee.EmployeeSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.EmployeeRepository;
import ca.mcgill.ecse321.opls.repository.EmployeeScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class TestEmployeeScheduleService {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeScheduleRepository employeeScheduleRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeScheduleService employeeScheduleService;

    private static final String jobTitle = "janitor", username = "krustykrab", password = "spongebobsquarepants",
            firstName = "spongebob", lastName = "squarepants", securityQuestion = "where do you work?",
            securityQuestionAnswer = "the krusty krab";

    private static final Double salary = 4.20;

    private static final UUID EMPLOYEE_UUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3"),
            NOT_EMPLOYEE_UUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f4");

    private static final int EMPLOYEE_USERACCOUNT_ID = 1234, NOT_EMPLOYEE_USERACCOUNT_ID = 5678;

    private static final Day day1 = Day.MONDAY, day2 = Day.FRIDAY, day3 = Day.SATURDAY;

    private static final Time startTime1 = Time.valueOf("10:10:10"), endTime1 = Time.valueOf("10:20:20"),
            startTime2 = Time.valueOf("20:20:20"), endTime2 = Time.valueOf("20:20:30");

    private Employee employee;

    private HashSet<EmployeeSchedule> scheduleHashSet;

    private UserAccount userAccount;

    private EmployeeSchedule employeeSchedule1, employeeSchedule2;

    @SuppressWarnings("unchecked")
	@BeforeEach
    public void setupMocks() {
        employee = spy(Employee.class);
        employee.setJobTitle(jobTitle);
        employee.setSalary(salary);

        userAccount = new UserAccount();
        userAccount.setUsername(username);
        userAccount.setFirstName(firstName);
        userAccount.setLastName(lastName);

        employee.setUserAccount(userAccount);

        Employee[] employees = new Employee[1];
        employees[0] = employee;

        employeeSchedule1 = new EmployeeSchedule();
        employeeSchedule1.setEmployee(employee);
        employeeSchedule1.setDay(day1);
        employeeSchedule1.setStartTime(startTime1);
        employeeSchedule1.setEndTime(endTime2);

        employeeSchedule2 = new EmployeeSchedule();
        employeeSchedule2.setEmployee(employee);
        employeeSchedule2.setDay(day2);
        employeeSchedule2.setStartTime(startTime2);
        employeeSchedule2.setEndTime(endTime2);

        ArrayList<EmployeeSchedule> employeeSchedules = new ArrayList<>();
        employeeSchedules.add(employeeSchedule1);
        employeeSchedules.add(employeeSchedule2);

        scheduleHashSet = mock(HashSet.class);

        try {
            userAccount.setPassword(password);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        userAccount.setSecurityAnswer(securityQuestion, securityQuestionAnswer);


        lenient().when(employeeRepository.findEmployeeByUuid(EMPLOYEE_UUID)).
                thenAnswer((InvocationOnMock invocation) -> employees[0]);

        lenient().when(employeeRepository.findEmployeeByUuid(NOT_EMPLOYEE_UUID)).
                thenAnswer((InvocationOnMock invocation) -> null);

        lenient().when(employeeRepository.findEmployeeByUserAccountId(EMPLOYEE_USERACCOUNT_ID)).
                thenAnswer((InvocationOnMock invocation) -> employees[0]);

        lenient().when(employeeRepository.findEmployeeByUserAccountId(NOT_EMPLOYEE_USERACCOUNT_ID)).
                thenAnswer((InvocationOnMock invocation) -> null);

        lenient().when(employeeScheduleRepository.findEmployeeScheduleByEmployee(employee)).
                thenAnswer((InvocationOnMock invocation) -> employeeSchedules);

        lenient().when(employeeScheduleRepository.findEmployeeScheduleByDay(any(), any())).
                thenAnswer((InvocationOnMock invocation) -> employeeSchedules.stream().filter(schedule ->
                        invocation.getArgument(1).equals(schedule.getDay())).findFirst().orElse(null));

        lenient().when(employeeScheduleRepository.save(any())).
                thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

        lenient().doAnswer((InvocationOnMock invocation) -> employeeSchedules.remove(invocation.getArgument(0))).
                when(employeeScheduleRepository).delete(any());

        lenient().when(employee.getScheduleSet()).thenAnswer((InvocationOnMock invocation) -> scheduleHashSet);

        lenient().when(scheduleHashSet.contains(any())).thenAnswer((InvocationOnMock invocation) -> true);

        lenient().when(scheduleHashSet.remove(any())).thenAnswer((InvocationOnMock invocation) ->
                employeeSchedules.remove(invocation.getArgument(0)));

        lenient().when(employeeService.getEmployee(EMPLOYEE_UUID)).
                thenAnswer((InvocationOnMock invocation) -> employee);

        lenient().when(employeeService.getEmployee(NOT_EMPLOYEE_UUID)).thenThrow(new OplsApiException(
                HttpStatus.NOT_FOUND, "not_found",
                "the employee with the given UUID does not exist!"));
    }

    @Test
    public void testGetEmployeeSchedule() {
        // test valid request
        EmployeeSchedule es = employeeScheduleService.getEmployeeSchedule(EMPLOYEE_UUID, day1);
        assertNotNull(es);
        assertEquals(day1, es.getDay());
        assertEquals(startTime1, es.getStartTime());
        assertEquals(endTime2, es.getEndTime());

        // test with invalid requests
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.getEmployeeSchedule(NOT_EMPLOYEE_UUID, day1));

        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.getEmployeeSchedule(EMPLOYEE_UUID, day3));
    }

    @Test
    public void testGetEmployeeAllSchedules() {
        // test valid requests with uuid and user account id
        List<EmployeeSchedule> schedules1 = employeeScheduleService.getEmployeeAllSchedules(EMPLOYEE_UUID);
        assertNotNull(schedules1);
        assertEquals(2, schedules1.size());
        assertTrue(schedules1.contains(employeeSchedule1));
        assertTrue(schedules1.contains(employeeSchedule2));

        List<EmployeeSchedule> schedules2 = employeeScheduleService.getEmployeeAllSchedules(EMPLOYEE_USERACCOUNT_ID);
        assertNotNull(schedules2);
        assertEquals(2, schedules2.size());
        assertTrue(schedules2.contains(employeeSchedule1));
        assertTrue(schedules2.contains(employeeSchedule2));

        // test with invalid uuid and user account id
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.getEmployeeAllSchedules(NOT_EMPLOYEE_UUID));

        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.getEmployeeAllSchedules(NOT_EMPLOYEE_USERACCOUNT_ID));
    }

    @Test
    public void testCreateEmployeeSchedule() {
        // test valid request
        EmployeeSchedule es = employeeScheduleService.createEmployeeSchedule(EMPLOYEE_UUID, day3, startTime1, endTime1);
        assertNotNull(es);
        assertEquals(day3, es.getDay());
        assertEquals(startTime1, es.getStartTime());
        assertEquals(endTime1, es.getEndTime());

        // test invalid requests
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> employeeScheduleService.createEmployeeSchedule(EMPLOYEE_UUID, day3, endTime1, startTime1));

        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.createEmployeeSchedule(NOT_EMPLOYEE_UUID, day3, startTime1, endTime1));

        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> employeeScheduleService.createEmployeeSchedule(EMPLOYEE_UUID, day2, startTime1, endTime1));
    }

    @Test
    public void testUpdateEmployeeSchedule() {
        // test valid request
        EmployeeSchedule es1 = employeeScheduleService.updateEmployeeSchedule(EMPLOYEE_UUID, day1, startTime2, endTime2);
        assertNotNull(es1);
        assertEquals(day1, es1.getDay());
        assertEquals(startTime2, es1.getStartTime());
        assertEquals(endTime2, es1.getEndTime());

        EmployeeSchedule es2 = employeeScheduleService.updateEmployeeSchedule(EMPLOYEE_UUID, day3, startTime2, endTime2);
        assertNotNull(es2);
        assertEquals(day3, es2.getDay());
        assertEquals(startTime2, es2.getStartTime());
        assertEquals(endTime2, es2.getEndTime());

        // test invalid requests
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> employeeScheduleService.updateEmployeeSchedule(EMPLOYEE_UUID, day1, endTime2, startTime2));

        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.updateEmployeeSchedule(NOT_EMPLOYEE_UUID, day1, startTime2, endTime2));
    }

    @Test
    public void testDeleteEmployeeSchedule() {
        // test valid request
        employeeScheduleService.deleteEmployeeSchedule(EMPLOYEE_UUID, day1);
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.getEmployeeSchedule(EMPLOYEE_UUID, day1));
        assertEquals(1, employeeScheduleService.getEmployeeAllSchedules(EMPLOYEE_UUID).size());

        // test invalid request
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeScheduleService.deleteEmployeeSchedule(NOT_EMPLOYEE_UUID, day2));

        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                () -> employeeScheduleService.deleteEmployeeSchedule(EMPLOYEE_UUID, day3));
    }
}
