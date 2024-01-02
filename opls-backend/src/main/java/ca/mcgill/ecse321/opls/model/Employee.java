package ca.mcgill.ecse321.opls.model;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.model.auth.UserAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Employee model.
 */
@Entity
public class Employee extends UuidModel {
	@Entity
	@Table(name = "employee_schedule", uniqueConstraints = {
			@UniqueConstraint(columnNames = {"employee_id", "day"})})
	public static class EmployeeSchedule extends Schedule {
		/**
		 * The schedule cannot navigate back to its Employee parent.
		 */
		@ManyToOne(optional = false)
		@JoinColumn(name = "employee_id", nullable = false)
		private Employee employee;

		public void setEmployee(Employee employee) {
			this.employee = employee;
		}
	}

	@OneToOne(optional = false, fetch = FetchType.EAGER)
	private UserAccount userAccount;

	@Column(nullable = false)
	private String jobTitle;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal salary = BigDecimal.ZERO;

	/**
	 * An Employee can view their shifts.
	 */
	@OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<EmployeeSchedule> schedules;

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public double getSalary() {
		return salary.doubleValue();
	}

	public void setSalary(double salary) {
		this.salary = BigDecimal.valueOf(salary);
	}

	public Iterable<EmployeeSchedule> getSchedules() {
		return schedules;
	}

	public Set<EmployeeSchedule> getScheduleSet() {
		return this.schedules;
	}

	public EmployeeSchedule addSchedule(Day day, Time startTime, Time endTime) {
		EmployeeSchedule s = new EmployeeSchedule();
		s.setDay(day);
		s.setStartTime(startTime);
		s.setEndTime(endTime);
		s.setEmployee(this);
		return s;
	}

	public EmployeeSchedule removeSchedule(Employee employee, Day day,
			Time startTime) {
		Iterable<EmployeeSchedule> s = employee.getSchedules();
		Iterator<EmployeeSchedule> iter = s.iterator();
		while (iter.hasNext()) {
			EmployeeSchedule currentSchedule = iter.next();
			if (currentSchedule.getDay() == day
					&& currentSchedule.getStartTime() == startTime) {
				((ArrayList<EmployeeSchedule>) s).remove(currentSchedule);
				return currentSchedule;
			}
		}
		return null;
	}

	public EmployeeSchedule removeSchedule(EmployeeSchedule es) {
		if (this.getScheduleSet().contains(es)) {
			this.getScheduleSet().remove(es);
			return es;
		}
		return null;
	}
}
