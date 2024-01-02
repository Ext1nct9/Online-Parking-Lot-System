package ca.mcgill.ecse321.opls.repository;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.opls.model.VehicleService;

/**
 * Repository to interface with the vehicle_service table.
 */
public interface VehicleServiceRepository
		extends
			CrudRepository<VehicleService, String> {

	/** Find a vehicle service by its String id. */
	VehicleService findVehicleServiceById(String id);

}
