package ca.mcgill.ecse321.opls.dto;

import java.util.UUID;

import org.hibernate.validator.constraints.Length;

import ca.mcgill.ecse321.opls.model.Employee;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request and response model for an employee.
 */
public class EmployeeDto {
	/**
	 * Employee UUID
	 */
	
	public UUID uuid;

	/**
	 * Job title of the employee
	 */
	@Length(min = 1, message = "The field must be at least 1 character!")
	@NotNull
	public String jobTitle;

	/**
	 * Salary of the employee
	 */
	@DecimalMin(value = "0.0", message = "The salary must be a positive number greater than 0.0!")
	@NotNull
	public Double salary;

	/**
	 * The UUID of the associated user account.
	 */
	@NotNull
	public UUID userAccountUUID;

	/** Default constructor. */
	public EmployeeDto() {
	}

	/** Constructor from fields. */
	public EmployeeDto(String jobTitle, Double salary, UUID uuid, UUID employeeUuid) {
		this.uuid = employeeUuid;
		this.jobTitle = jobTitle;
		this.salary = salary;
		this.userAccountUUID = uuid;
	}

	/** Constructor from the database entity. */
	public EmployeeDto(Employee employee) {
		this.uuid = employee.getUuid();
		this.jobTitle = employee.getJobTitle();
		this.salary = employee.getSalary();
		this.userAccountUUID = employee.getUserAccount().getUuid();
	}

}
