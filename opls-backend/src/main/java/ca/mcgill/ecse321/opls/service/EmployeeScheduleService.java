package ca.mcgill.ecse321.opls.service;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.Employee;
import ca.mcgill.ecse321.opls.model.Employee.EmployeeSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.repository.EmployeeRepository;
import ca.mcgill.ecse321.opls.repository.EmployeeScheduleRepository;
import ca.mcgill.ecse321.opls.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
public class EmployeeScheduleService {

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    EmployeeScheduleRepository employeeScheduleRepository;

    /**
     * Retrieves the schedule for the employee with the given UUID, for the given day.
     * If no schedule for the given day exists, throws an API exception.
     * @param uuid          UUID of the employee whose schedule is returned
     * @param day           day of the week of the schedule
     * @return              schedule of the employee for the given day
     */
    @Transactional
    public EmployeeSchedule getEmployeeSchedule(UUID uuid, Day day) {
        Employee employee = employeeService.getEmployee(uuid);

        for (EmployeeSchedule es: employeeScheduleRepository.findEmployeeScheduleByEmployee(employee)) {
            if (es.getDay().equals(day)) {
                return es;
            }
        }

        throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                "no schedule for the given employee and day exist!");
    }

    /**
     * Retrieves all schedules for the employee with the given UUID.
     * @param uuid          UUID of the employee whose schedules are returned
     * @return              list of the employee's schedules
     */
    @Transactional
    public List<EmployeeSchedule> getEmployeeAllSchedules(UUID uuid) {
        Employee employee = employeeService.getEmployee(uuid);

        ArrayList<EmployeeSchedule> schedules = new ArrayList<>();

        for (EmployeeSchedule es: employeeScheduleRepository.findEmployeeScheduleByEmployee(employee)) {
            schedules.add(es);
        }

        return schedules;
    }

    /**
     * Retrieves all schedules for the employee with the given user account id.
     * @param userAccountId     user account id of the employee whose schedules are returned
     * @return                  list of the employee's schedules
     */
    @Transactional
    public List<EmployeeSchedule> getEmployeeAllSchedules(int userAccountId) {
        Employee employee = employeeRepository.findEmployeeByUserAccountId(userAccountId);

        if (isNull(employee)) {
            throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                    "the employee with the given UUID does not exist!");
        }

        ArrayList<EmployeeSchedule> schedules = new ArrayList<>();

        for (EmployeeSchedule es: employeeScheduleRepository.findEmployeeScheduleByEmployee(employee)) {
            schedules.add(es);
        }

        return schedules;
    }

    /**
     * Creates a schedule for the employee with the given UUID for the given day, with the given start and end times.
     * If the start time is after the end time, or if a schedule for the employee for the given day already exists,
     * throws an API exception.
     * @param uuid              UUID of the employee whose schedule is created
     * @param day               day of the week of the schedule to create
     * @param startTime         start time of the schedule
     * @param endTime           end time of the schedule
     * @return                  newly created schedule
     */
    @Transactional
    public EmployeeSchedule createEmployeeSchedule(UUID uuid, Day day, Time startTime, Time endTime) {
        if (startTime.after(endTime)) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "the start time is after the end time!");
        }

        Employee employee = employeeService.getEmployee(uuid);

        for (EmployeeSchedule es: employeeScheduleRepository.findEmployeeScheduleByEmployee(employee)) {
            if (es.getDay().equals(day)) {
                throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                        "an employee schedule for this day already exists!");
            }
        }

        return employeeScheduleRepository.save(employee.addSchedule(day, startTime, endTime));
    }

    /**
     * Updates the schedule of the given employee for the given day with the given start and end times.
     * If the start time is after the end time, throws an API exception.
     * If the employee does not have a schedule for the given day, creates a new schedule with the given parameters.
     * @param uuid              UUID of the employee whose schedule is updated
     * @param day               day of the week of the schedule to update
     * @param startTime         new start time of the schedule
     * @param endTime           new end time of the schedule
     * @return                  newly updated schedule
     */
    @Transactional
    public EmployeeSchedule updateEmployeeSchedule(UUID uuid, Day day, Time startTime, Time endTime) {
        if (startTime.after(endTime)) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "the start time is after the end time!");
        }

        Employee employee = employeeService.getEmployee(uuid);

        for (EmployeeSchedule es: employeeScheduleRepository.findEmployeeScheduleByEmployee(employee)) {
            if (es.getDay().equals(day)) {
                es.setStartTime(startTime);
                es.setEndTime(endTime);
                return employeeScheduleRepository.save(es);
            }
        }

        return employeeScheduleRepository.save(employee.addSchedule(day, startTime, endTime));
    }

    /**
     * Deletes the schedule of the given employee for the given day.
     * If the schedule does not exist, throws an API exception.
     * @param uuid              UUID of the employee whose schedule is deleted
     * @param day               day of the week of the schedule to delete
     */
    @Transactional
    public EmployeeSchedule deleteEmployeeSchedule(UUID uuid, Day day) {
        Employee employee = employeeService.getEmployee(uuid);

        EmployeeSchedule employeeSchedule = employeeScheduleRepository.findEmployeeScheduleByDay(employee, day);

        if (isNull(employeeSchedule)) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "no schedule for the given employee and day exist!");
        }

        employee.removeSchedule(employeeSchedule);
        employeeScheduleRepository.delete(employeeSchedule);
        return employeeSchedule;
    }
}
