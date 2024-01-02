package ca.mcgill.ecse321.opls.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;

public interface EmployeeRepository extends CrudRepository<Employee, Integer> {
	
	/** Get all employees. */
	@Query(value = "SELECT e FROM Employee e")
	Iterable<Employee> getEmployees();

	/** Find an employee by integer id. */
	Employee findEmployeeById(int id);

	/** Find an employee by UUID. */
	Employee findEmployeeByUuid(UUID uuid);
	
    /** Find an employee associated with a UserAccount. */
    Employee findEmployeeByUserAccount(UserAccount userAccount);
    
    /** Find an employee associated with a UserAccount. */
    default Employee findEmployeeByUserAccountId(int userAccountId) {
    	var userAccount = new UserAccount();
    	userAccount.overrideId(userAccountId);
    	return findEmployeeByUserAccount(userAccount);
    }
    
}
