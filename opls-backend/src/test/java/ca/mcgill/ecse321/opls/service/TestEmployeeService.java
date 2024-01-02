package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.EmployeeRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
public class TestEmployeeService {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private UserAccountClaimRepository userAccountClaimRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private static final String jobTitle = "janitor", username = "krustykrab", password = "spongebobsquarepants",
            firstName = "spongebob", lastName = "squarepants", securityQuestion = "where do you work?",
            securityQuestionAnswer = "the krusty krab";

    private static final Double salary = 4.20;

    private static final UUID EMPLOYEE_UUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3"),
            NOT_EMPLOYEE_UUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f4"),
            NOT_USER_ACCOUNT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final int EMPLOYEE_USERACCOUNT_ID = 1234, NOT_EMPLOYEE_USERACCOUNT_ID = 5678;

    private Employee employee;

    private UserAccount userAccount, userAccountNew;

    private UUID userAccountUUID, userAccountUUIDNew;

    @BeforeEach
    public void setupMocks() {
        employee = new Employee();
        employee.setJobTitle(jobTitle);
        employee.setSalary(salary);

        userAccount = new UserAccount();
        userAccount.setUsername(username);
        userAccount.setFirstName(firstName);
        userAccount.setLastName(lastName);

        employee.setUserAccount(userAccount);

        Employee[] employees = new Employee[1];
        employees[0] = employee;


        try {
            userAccount.setPassword(password);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        userAccount.setSecurityAnswer(securityQuestion, securityQuestionAnswer);

        userAccountNew = new UserAccount();
        userAccountNew.setFirstName("patrick");
        userAccountNew.setLastName("star");
        userAccountNew.setUsername("patrickstar");
        try {
            userAccountNew.setPassword(password);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        userAccountNew.setSecurityAnswer("i ran out", "of ideas");

        userAccountUUID = userAccount.getUuid();

        userAccountUUIDNew = userAccountNew.getUuid();

        lenient().when(employeeRepository.findEmployeeByUuid(EMPLOYEE_UUID)).
                thenAnswer((InvocationOnMock invocation) -> employees[0]);

        lenient().when(employeeRepository.findEmployeeByUuid(NOT_EMPLOYEE_UUID)).
                thenAnswer((InvocationOnMock invocation) -> null);

        lenient().when(employeeRepository.findEmployeeByUserAccountId(EMPLOYEE_USERACCOUNT_ID)).
                thenAnswer((InvocationOnMock invocation) -> employees[0]);

        lenient().when(employeeRepository.findEmployeeByUserAccountId(NOT_EMPLOYEE_USERACCOUNT_ID)).
                thenAnswer((InvocationOnMock invocation) -> null);

        lenient().when(employeeRepository.save(any())).
                thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

        lenient().doAnswer((InvocationOnMock invocation) -> employees[0] = null).
                when(employeeRepository).delete(employee);

        lenient().when(userAccountRepository.findUserAccountByUuid(userAccountUUID)).
                thenAnswer((InvocationOnMock invocation) -> userAccount);

        lenient().when(userAccountRepository.findUserAccountByUuid(userAccountUUIDNew)).
                thenAnswer((InvocationOnMock invocation) -> userAccountNew);

        lenient().when(userAccountService.getUserAccount(userAccountUUID)).
               thenAnswer((InvocationOnMock invocation) -> userAccount);

        lenient().when(userAccountService.getUserAccount(userAccountUUIDNew)).
                thenAnswer((InvocationOnMock invocation) -> userAccountNew);

        lenient().when(userAccountService.getUserAccount(NOT_USER_ACCOUNT_UUID)).
                thenThrow(new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                "the user account with the given UUID does not exist!"));

        lenient().when(userAccountClaimRepository.save(any())).
                thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));
    }

    @Test
    public void testGetEmployee() {
        // test valid request using uuid
        Employee e1 = employeeService.getEmployee(EMPLOYEE_UUID);
        assertNotNull(e1);
        assertEquals(userAccount, e1.getUserAccount());
        assertEquals(jobTitle, e1.getJobTitle());
        assertEquals(salary, e1.getSalary());

        // test valid request using user account id
        Employee e3 = employeeService.getEmployee(EMPLOYEE_USERACCOUNT_ID);
        assertNotNull(e3);
        assertEquals(userAccount, e3.getUserAccount());
        assertEquals(jobTitle, e3.getJobTitle());
        assertEquals(salary, e3.getSalary());

        // test invalid request using uuid
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeService.getEmployee(NOT_EMPLOYEE_UUID));

        // test invalid request using user account id
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeService.getEmployee(NOT_EMPLOYEE_USERACCOUNT_ID));
    }

    @Test
    public void testCreateEmployee() {
        // test valid request
        Employee e1 = employeeService.createEmployee(userAccount.getUuid(), jobTitle, salary);
        assertNotNull(e1);
        assertEquals(userAccount, e1.getUserAccount());
        assertEquals(jobTitle, e1.getJobTitle());
        assertEquals(salary, e1.getSalary());

        // test request with invalid user account uuid
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeService.createEmployee(UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        jobTitle, salary));

		// test request for user account with an existing employee
		lenient()
				.when(employeeRepository
						.findEmployeeByUserAccountId(userAccount.getId()))
				.thenAnswer((InvocationOnMock invocation) -> e1);
		assertThrowsApiException(HttpStatus.CONFLICT,
				"An employee is already associated with this user account.",
				() -> employeeService.createEmployee(userAccount.getUuid(),
						jobTitle, salary));
    }

    @Test
    public void testUpdateEmployee() {
        // test valid request
        Employee e1 = employeeService.updateEmployee(EMPLOYEE_UUID, "ceo", 1234567.89);
        assertNotNull(e1);
        assertEquals("ceo", e1.getJobTitle());
        assertEquals(1234567.89, e1.getSalary());
    }

    @Test
    public void testSetEmployeeAttributes() {
        // test valid requests
                Employee e1 = employeeService.setEmployeeUserAccount(EMPLOYEE_UUID, userAccountNew.getUuid());
        assertNotNull(e1);
        assertEquals(userAccountNew, e1.getUserAccount());

        Employee e2 = employeeService.setEmployeeJobTitle(EMPLOYEE_UUID, "ceo");
        assertNotNull(e2);
        assertEquals("ceo", e2.getJobTitle());

        Employee e3 = employeeService.setEmployeeSalary(EMPLOYEE_UUID, 1234567.89);
        assertNotNull(e3);
        assertEquals(1234567.89, e3.getSalary());
    }

    @Test
    public void testDeleteEmployee() {
        // test valid request
        employeeService.deleteEmployee(EMPLOYEE_UUID);
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeService.getEmployee(EMPLOYEE_UUID));

        // test invalid request
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found",
                () -> employeeService.deleteEmployee(NOT_EMPLOYEE_UUID));
    }
}