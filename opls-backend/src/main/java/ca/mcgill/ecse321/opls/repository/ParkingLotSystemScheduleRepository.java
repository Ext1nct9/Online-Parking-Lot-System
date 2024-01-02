package ca.mcgill.ecse321.opls.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem.ParkingLotSystemSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;

/**
 * Repository to interface with the parking_lot_system_schedule table.
 */
public interface ParkingLotSystemScheduleRepository extends CrudRepository<ParkingLotSystemSchedule, Integer> {

	/** Find a schedule element by its numerical id. */
    ParkingLotSystemSchedule findParkingLotSystemScheduleById(Integer id);
    
    /** Get all the schedules for the active parking lot system. */
    @Query(value = "SELECT sched.* FROM parking_lot_system_schedule sched INNER JOIN parking_lot_system sys ON sys.id = sched.parking_lot_system_id AND sys.is_active", nativeQuery = true)
    Iterable<ParkingLotSystemSchedule> findActiveParkingLotSchedules();
    
    /** Get all the schedules on a specified day for the active parking lot system. */
    @Query(value = "SELECT sched.* FROM parking_lot_system_schedule sched INNER JOIN parking_lot_system sys ON sys.id = sched.parking_lot_system_id AND sys.is_active AND sched.day = :day", nativeQuery = true)
    Iterable<ParkingLotSystemSchedule> findActiveParkingLotSchedulesByDay(@Param("day") String day);
    
    /** Get all the schedules on a specified day for the active parking lot system. */
    default Iterable<ParkingLotSystemSchedule> findActiveParkingLotSchedulesByDay(Day day) {
    	return findActiveParkingLotSchedulesByDay(day.toString());
    }
    
    /** Get the first schedule on a specified day for the active parking lot system. */
    default ParkingLotSystemSchedule findActiveParkingLotScheduleByDay(Day day) {
    	for (var sched : findActiveParkingLotSchedulesByDay(day.toString())) {
    		return sched;
    	}
    	
    	return null;
    }

}
