package ca.mcgill.ecse321.opls.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem;

/**
 * Repository to interface with the parking_lot_system table.
 */
public interface ParkingLotSystemRepository extends CrudRepository<ParkingLotSystem, String> {
	
	/** Get the parking lot system configuration that is active. */
	@Query(value = "SELECT p FROM ParkingLotSystem p WHERE p.isActive")
	ParkingLotSystem getActiveParkingLotSystem();
	
	/** Get a parking lot system configuration by the string id value. */
	ParkingLotSystem findParkingLotSystemById(String id);
	
}
