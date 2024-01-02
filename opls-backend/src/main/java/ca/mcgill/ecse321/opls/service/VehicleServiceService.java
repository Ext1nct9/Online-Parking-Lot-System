package ca.mcgill.ecse321.opls.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.repository.VehicleServiceRepository;

@Service
public class VehicleServiceService {

	@Autowired
	private VehicleServiceRepository vehicleServiceRepository;
	

	
	/**
	 * Method to retrieve a vehicle service by id
	 * @param id of vehicle service
	 * @return vehicle service
	 */
	@Transactional
	public VehicleService getVehicleService(String id) {
		if (id.length() == 0) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "id cannot be empty");
        }

		if (vehicleServiceRepository.findVehicleServiceById(id) == null) {
            throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                    "the Vehicle Service with the provided id does not exist");
        }
		
		return vehicleServiceRepository.findVehicleServiceById(id);

	}

	/**
	 * Method to retrieve all vehicle services
	 * @return list of vehicle services
	 */
	@Transactional
	public Iterable<VehicleService> getAllVehicleServices() {
		return vehicleServiceRepository.findAll();
	}


	/**
	 * Method to create a vehicle service
	 * @param displayName
	 * @param duration 
	 * @param fee
	 * @param startDate
	 * @return the created vehicle service
	 */
	@Transactional
	public VehicleService createVehicleService(String displayName, int duration, double fee) {

		VehicleService vs = new VehicleService();
		vs.setDisplayName(displayName);
		vs.setDuration(duration);
		vs.setFee(fee);

		return vehicleServiceRepository.save(vs);

	}

	/**
     * Updates the vehicle service with the given id with the given display name, duration and fee.
	 * @param id
     * @param displayName            
     * @param duration     
     * @param fee        
     */
    @Transactional
    public VehicleService updateVehicleService(String id, String displayName, int duration, double fee) {

        VehicleService vs = getVehicleService(id);
        vs.setDisplayName(displayName);
        vs.setDuration(duration);
        vs.setFee(fee);
        return vehicleServiceRepository.save(vs);
    }


	/**
	 * Method to delete a vehicle service
	 * @param id 
	 */
	@Transactional
	public void deleteVehicleService(String id){
		if (id.length() == 0) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "id cannot be empty");
        }

		vehicleServiceRepository.delete(getVehicleService(id));
	}

}