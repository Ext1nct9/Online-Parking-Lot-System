package ca.mcgill.ecse321.opls.repository;

import ca.mcgill.ecse321.opls.model.Schedule.Day;
import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.Employee.EmployeeSchedule;

public interface EmployeeScheduleRepository
		extends
			CrudRepository<EmployeeSchedule, Integer> {

	Iterable <EmployeeSchedule> findEmployeeScheduleByEmployee(Employee Employee);

	default EmployeeSchedule findEmployeeScheduleByDay(Employee employee, Day day) {
		for (EmployeeSchedule sched : findEmployeeScheduleByEmployee(employee)) {
			if (sched.getDay().equals(day)) {
				return sched;
			}
		}

		return null;
	}

}