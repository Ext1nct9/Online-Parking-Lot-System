package ca.mcgill.ecse321.opls.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotDto;
import ca.mcgill.ecse321.opls.dto.spot.ParkingSpotQueryRequestDto;
import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.repository.ParkingSpotRepository;
import jakarta.transaction.Transactional;

@Service
public class ParkingSpotService {

	@Autowired
	private ParkingSpotRepository spotRepository;

	/**
	 * Create a parking spot.
	 * 
	 * @param request
	 *            The parking spot entity.
	 * @return The saved entity.
	 */
	@Transactional
	public ParkingSpot createParkingSpot(ParkingSpot request) {
		return spotRepository.save(request);
	}

	/**
	 * Fetch a parking spot.
	 * 
	 * @param spotId
	 *            The ID of the parking spot.
	 * @return The ParkingSpot entity.
	 * @throws OplsApiException
	 *             if the spot is not found.
	 */
	public ParkingSpot getParkingSpot(String spotId) {
		// get the spot
		var spot = spotRepository.findParkingSpotById(spotId);
		if (spot == null) {
			throw new OplsApiException(HttpStatus.NOT_FOUND,
					"Parking spot not found.");
		}

		return spot;
	}

	/**
	 * Query parking spots.
	 * 
	 * @param queryReq
	 *            The query parameters.
	 * @return The queried results
	 */
	public Iterable<ParkingSpot> query(ParkingSpotQueryRequestDto queryReq) {
		Iterable<ParkingSpot> results = null;

		// convert Character list to char array
		char[] floors = null;
		if (queryReq.floors != null) {
			floors = new char[queryReq.floors.size()];
			int i = 0;
			for (Character c : queryReq.floors) {
				floors[i++] = c;
			}
		}

		// execute query
		if (queryReq.unbooked) {
			results = spotRepository.queryUnbooked(floors, queryReq.statuses,
					queryReq.vehicleTypes);
		} else {
			results = spotRepository.query(floors, queryReq.statuses,
					queryReq.vehicleTypes);
		}

		return results;
	}
	
	/**
	 * Query count of parking spots.
	 * 
	 * @param queryReq
	 *            The query parameters.
	 * @return The number of queried results.
	 */
	public int queryCount(ParkingSpotQueryRequestDto queryReq) {
		int count = 0;

		// convert Character list to char array
		char[] floors = null;
		if (queryReq.floors != null) {
			floors = new char[queryReq.floors.size()];
			int i = 0;
			for (Character c : queryReq.floors) {
				floors[i++] = c;
			}
		}

		// execute query
		if (queryReq.unbooked) {
			count = spotRepository.queryUnbookedCount(floors, queryReq.statuses, queryReq.vehicleTypes);
		} else {
			count = spotRepository.queryCount(floors, queryReq.statuses, queryReq.vehicleTypes);
		}

		return count;
	}
	
	/**
	 * Update a parking spot.
	 *
	 * @param id spot id
	 * @param request
	 *            The parameters of the updated spot.
	 * @return The ParkingSpot entity.
	 */
	@Transactional
	public ParkingSpot updateParkingSpot(String id, ParkingSpotDto request){
		ParkingSpot fetchedParkingSpot = getParkingSpot(id);
		spotRepository.delete(fetchedParkingSpot);
		ParkingSpot newParkingSpot = request.toModel();
		spotRepository.save(newParkingSpot);
		return newParkingSpot;
	}
	/**
	 * Delete a parking spot.
	 *
	 * @param id spot id.
	 */
	@Transactional
	public ParkingSpot deleteParkingSpot(String id){
		ParkingSpot fetchedParkingSpot = getParkingSpot(id);
		spotRepository.delete(fetchedParkingSpot);
		return fetchedParkingSpot;
	}

}
