package ca.mcgill.ecse321.opls.service;

import static java.util.Objects.isNull;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.auth.OAuthClaim;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import ca.mcgill.ecse321.opls.repository.EmployeeRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountClaimRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    UserAccountClaimRepository userAccountClaimRepository;

    @Autowired
    UserAccountService userAccountService;
    
    /**
     * Get all employees.
     */
    public Iterable<Employee> getEmployees() {
    	return employeeRepository.getEmployees();
    }

    /**
     * Retrieves the employee with the given UUID.
     * @param uuid      UUID of the employee to get
     * @return          employee with the given UUID
     */
    @Transactional
    public Employee getEmployee(UUID uuid) {
        Employee employee = employeeRepository.findEmployeeByUuid(uuid);

        if (isNull(employee)) {
            throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                    "the employee with the given UUID does not exist!");
        }

        return employee;
    }

    /**
     * Retrieves the employee with the given user account id.
     * @param userAccountId     UUID of the employee to get
     * @return                  employee with the given UUID
     */
    @Transactional
    public Employee getEmployee(int userAccountId) {
        Employee employee = employeeRepository.findEmployeeByUserAccountId(userAccountId);

        if (isNull(employee)) {
            throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                    "the employee with the given user account UUID does not exist!");
        }

        return employee;
    }

	/**
	 * Associate a user account and an employee.
	 * 
	 * @param userAccountUUID
	 *            UUID of the user account.
	 * @param employee
	 *            The employee entity.
	 * @throws OplsApiException
	 *             if the user account is already associated with an employee.
	 */
	private void associateUserWithEmployee(UUID userAccountUUID,
			Employee employee) {
		UserAccount ua = userAccountService.getUserAccount(userAccountUUID);

		Employee existingEmployee = null;
		try {
			existingEmployee = getEmployee(ua.getId());
		} catch (Exception e) {
		}

		if (existingEmployee != null) {
			throw new OplsApiException(HttpStatus.CONFLICT,
					"An employee is already associated with this user account.");
		}

		if (!userAccountClaimRepository.userHasClaim(ua, OAuthClaim.EMPLOYEE)) {
			userAccountClaimRepository
					.save(ua.addUserAccountClaim(OAuthClaim.EMPLOYEE));
		}

		employee.setUserAccount(ua);
	}

    /**
     * Creates a new employee with the given job title and salary.
     * If the user account is invalid, throws an API exception.
     * @param userAccountUUID   user account to be associated with the new employee
     * @param jobTitle          job title of new employee
     * @param salary            salary of new employee
     * @return                  new employee
     */
	@Transactional
	public Employee createEmployee(UUID userAccountUUID, String jobTitle,
			Double salary) {
		Employee e = new Employee();
		e.setJobTitle(jobTitle);
		e.setSalary(salary);

		associateUserWithEmployee(userAccountUUID, e);

		return employeeRepository.save(e);
	}

    /**
     * Updates the employee with the given UUID with the given job title and salary.
     * @param uuid          UUID of the employee to update
     * @param jobTitle      new job title
     * @param salary        new salary
     */
    @Transactional
    public Employee updateEmployee(UUID uuid, String jobTitle, Double salary) {
        Employee e = employeeRepository.findEmployeeByUuid(uuid);
        
        if (jobTitle != null) {
        	e.setJobTitle(jobTitle);
        }
        
        if (salary != null) {
        	e.setSalary(salary);
        }
        
        return employeeRepository.save(e);
    }

    /**
     * Updates the employee with the given UUID with the given user account.
     * If the parameters are null, throws an API exception.
     * @param employeeUUID          UUID of the employee to update
     * @param userAccountUUID       UUID of the new user account
     */
	@Transactional
	public Employee setEmployeeUserAccount(UUID employeeUUID,
			UUID userAccountUUID) {
		Employee e = employeeRepository.findEmployeeByUuid(employeeUUID);

		associateUserWithEmployee(userAccountUUID, e);

		return employeeRepository.save(e);
	}

    /**
     * Updates the employee with the given UUID with the given job title.
     * If the job title is invalid, throws an API exception.
     * @param uuid          UUID of the employee to update
     * @param jobTitle      new job title
     */
    @Transactional
    public Employee setEmployeeJobTitle(UUID uuid, String jobTitle) {
        Employee e = employeeRepository.findEmployeeByUuid(uuid);
        e.setJobTitle(jobTitle);
        return employeeRepository.save(e);
    }

    /**
     * Updates the employee with the given UUID with the given salary.
     * If the salary is negative, throws an API exception.
     * @param uuid          UUID of the employee to update
     * @param salary        new salary
     */
    @Transactional
    public Employee setEmployeeSalary(UUID uuid, Double salary) {
        Employee e = employeeRepository.findEmployeeByUuid(uuid);
        e.setSalary(salary);
        return employeeRepository.save(e);
    }

    /**
     * Deletes the employee with the given UUID
     * @param uuid          UUID of employee to delete
     */
	@Transactional
	public Employee deleteEmployee(UUID uuid) {
		var employee = getEmployee(uuid);
		employeeRepository.delete(employee);
		return employee;
	}

}