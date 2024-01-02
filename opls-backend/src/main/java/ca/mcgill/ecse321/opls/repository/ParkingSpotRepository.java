package ca.mcgill.ecse321.opls.repository;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.opls.model.ParkingSpot;
import ca.mcgill.ecse321.opls.model.ParkingSpot.ParkingSpotStatus;
import ca.mcgill.ecse321.opls.model.ParkingSpot.VehicleType;

/**
 * Saved queries for the parking_spot table.
 */
class ParkingSpotQueryHelper {
	
	private static final String BASE_QUERY_WHERE_CLAUSE = "(:floors IS NULL OR :floors = '' OR id ~* :floors) AND "
			+ "parking_spot_status IN :statuses AND "
			+ "vehicle_type IN :vehicleTypes";

	/*
	 * Query parking_spot table with parameters.
	 * 
	 * @param :floors the String containing target floors, null if query all.
	 * @param :statuses the String array of ParkingSpotStatus values.
	 * @param :vehicleTypes the String array of VehicleType values.
	 */
	public static final String BASE_QUERY = "SELECT * FROM parking_spot WHERE "
			+ "(:floors IS NULL OR :floors = '' OR id ~* :floors) AND "
			+ "parking_spot_status IN :statuses AND "
			+ "vehicle_type IN :vehicleTypes";
	
	/*
	 * Query count of parking_spot table with parameters.
	 * 
	 * @param :floors the String containing target floors, null if query all.
	 * @param :statuses the String array of ParkingSpotStatus values.
	 * @param :vehicleTypes the String array of VehicleType values.
	 */
	public static final String BASE_QUERY_COUNT = "SELECT COUNT(1) FROM parking_spot ps WHERE "
			+ BASE_QUERY_WHERE_CLAUSE;

	private static final String NO_BOOKINGS_WHERE_CLAUSE = "(:floors IS NULL OR :floors = '' OR ps.id ~* :floors) AND "
			+ "ps.parking_spot_status IN :statuses AND "
			+ "ps.vehicle_type IN :vehicleTypes AND "
			+ "NOT EXISTS (SELECT 1 FROM parking_spot_booking WHERE parking_spot_id = ps.id AND start_date <= NOW() AND end_date >= NOW())";

	/*
	 * Query parking_spot table with parameters. Includes a query to the
	 * parking_spot_booking table to determine if the spot is reserved.
	 * 
	 * @param :floors the String containing target floors, null if query all.
	 * @param :statuses the String array of ParkingSpotStatus values.
	 * @param :vehicleTypes the String array of VehicleType values.
	 */
	public static final String NO_BOOKINGS_QUERY = "SELECT * FROM parking_spot ps WHERE "
			+ NO_BOOKINGS_WHERE_CLAUSE;

	/*
	 * Query count of parking_spot table entries with parameters. Includes a
	 * query to the parking_spot_booking table to determine if the spot is
	 * reserved.
	 * 
	 * @param :floors the String containing target floors, null if query all.
	 * @param :statuses the String array of ParkingSpotStatus values.
	 * @param :vehicleTypes the String array of VehicleType values.
	 */
	public static final String NO_BOOKINGS_QUERY_COUNT = "SELECT COUNT(1) FROM parking_spot ps WHERE "
			+ NO_BOOKINGS_WHERE_CLAUSE;

	/** Convert a list of queried floors to a regex string. */
	public static String floorsListToString(char[] floors) {
		String floorsStr = null;
		if (floors != null && floors.length > 0) {
			floorsStr = "[" + String.copyValueOf(floors) + "][0-9]{3}";
		}
		return floorsStr;
	}

	/** Convert each enum value to a String. */
	public static String[] statusList(Collection<ParkingSpotStatus> statuses) {
		if (statuses == null || statuses.size() == 0) {
			statuses = Arrays.asList(ParkingSpotStatus.values());
		}
		String[] statusList = new String[statuses.size()];
		int i = 0;
		for (var status : statuses) {
			statusList[i++] = status.toString();
		}
		return statusList;
	}

	/** Convert each enum value to a String. */
	public static String[] vehicleTypeList(
			Collection<VehicleType> vehicleTypes) {
		if (vehicleTypes == null || vehicleTypes.size() == 0) {
			vehicleTypes = Arrays.asList(VehicleType.values());
		}
		String[] typeList = new String[vehicleTypes.size()];
		int i = 0;
		for (var type : vehicleTypes) {
			typeList[i++] = type.toString();
		}
		return typeList;
	}

}

/**
 * Repository to interface with the parking_spot table.
 */
public interface ParkingSpotRepository
		extends
			CrudRepository<ParkingSpot, String> {

	/** Find a parking spot by its String identifier. */
	ParkingSpot findParkingSpotById(String id);

	/** Find a parking spot on a floor and spot number. */
	@Query(value = "SELECT * FROM parking_spot WHERE id = CONCAT(:floor, LPAD(CAST(:spot AS VARCHAR), 3, '0'))", nativeQuery = true)
	ParkingSpot findParkingSpotById(@Param("floor") char floor,
			@Param("spot") int spot);

	/**
	 * Query for parking spots. WARNING: this method does not accept null for
	 * statuses and vehicleTypes.
	 * 
	 * @param floors
	 *            Regex string containing target floors.
	 * @param statuses
	 *            List of statuses to match. CANNOT BE NULL.
	 * @param vehicleTypes
	 *            List of vehicle types to match. CANNOT BE NULL.
	 */
	@Query(value = ParkingSpotQueryHelper.BASE_QUERY, nativeQuery = true)
	Iterable<ParkingSpot> queryExact(@Param("floors") String floors,
			@Param("statuses") String[] statuses,
			@Param("vehicleTypes") String[] vehicleTypes);
	
	/**
	 * Query count of parking spots. WARNING: this method does not accept null for
	 * statuses and vehicleTypes.
	 * 
	 * @param floors
	 *            Regex string containing target floors.
	 * @param statuses
	 *            List of statuses to match. CANNOT BE NULL.
	 * @param vehicleTypes
	 *            List of vehicle types to match. CANNOT BE NULL.
	 */
	@Query(value = ParkingSpotQueryHelper.BASE_QUERY_COUNT, nativeQuery = true)
	int queryExactCount(@Param("floors") String floors,
			@Param("statuses") String[] statuses,
			@Param("vehicleTypes") String[] vehicleTypes);

	/**
	 * Query for parking spots without bookings. WARNING: this method does not
	 * accept null for statuses and vehicleTypes.
	 * 
	 * @param floors
	 *            Regex string containing target floors.
	 * @param statuses
	 *            List of statuses to match. CANNOT BE NULL.
	 * @param vehicleTypes
	 *            List of vehicle types to match. CANNOT BE NULL.
	 */
	@Query(value = ParkingSpotQueryHelper.NO_BOOKINGS_QUERY, nativeQuery = true)
	Iterable<ParkingSpot> queryUnbookedExact(@Param("floors") String floors,
			@Param("statuses") String[] statuses,
			@Param("vehicleTypes") String[] vehicleTypes);

	/**
	 * Query number of parking spots without bookings. WARNING: this method does
	 * not accept null for statuses and vehicleTypes.
	 * 
	 * @param floors
	 *            Regex string containing target floors.
	 * @param statuses
	 *            List of statuses to match. CANNOT BE NULL.
	 * @param vehicleTypes
	 *            List of vehicle types to match. CANNOT BE NULL.
	 */
	@Query(value = ParkingSpotQueryHelper.NO_BOOKINGS_QUERY_COUNT, nativeQuery = true)
	int queryUnbookedExactCount(@Param("floors") String floors,
			@Param("statuses") String[] statuses,
			@Param("vehicleTypes") String[] vehicleTypes);

	/**
	 * Query for parking spots.
	 * 
	 * @param floors
	 *            The floors to look on, empty for all.
	 * @param statuses
	 *            The statuses to query, null or empty to query all.
	 * @param vehicleTypes
	 *            The vehicle sizes to query, null or empty to query all.
	 * @return Queried results.
	 */
	default Iterable<ParkingSpot> query(char[] floors,
			Collection<ParkingSpotStatus> statuses,
			Collection<VehicleType> vehicleTypes) {
		String floorsStr = ParkingSpotQueryHelper.floorsListToString(floors);
		String[] statusList = ParkingSpotQueryHelper.statusList(statuses);
		String[] typeList = ParkingSpotQueryHelper
				.vehicleTypeList(vehicleTypes);

		return queryExact(floorsStr, statusList, typeList);
	}
	
	/**
	 * Query count of parking spots.
	 * 
	 * @param floors
	 *            The floors to look on, empty for all.
	 * @param statuses
	 *            The statuses to query, null or empty to query all.
	 * @param vehicleTypes
	 *            The vehicle sizes to query, null or empty to query all.
	 * @return Number of queried results.
	 */
	default int queryCount(char[] floors,
			Collection<ParkingSpotStatus> statuses,
			Collection<VehicleType> vehicleTypes) {
		String floorsStr = ParkingSpotQueryHelper.floorsListToString(floors);
		String[] statusList = ParkingSpotQueryHelper.statusList(statuses);
		String[] typeList = ParkingSpotQueryHelper
				.vehicleTypeList(vehicleTypes);

		return queryExactCount(floorsStr, statusList, typeList);
	}

	/**
	 * Query for parking spots without active bookings.
	 * 
	 * @param floors
	 *            The floors to look on, null or empty to query all.
	 * @param statuses
	 *            The statuses to query, null or empty to query all.
	 * @param vehicleTypes
	 *            The vehicle sizes to query, null or empty to query all.
	 * @return Queried results.
	 */
	default Iterable<ParkingSpot> queryUnbooked(char[] floors,
			Collection<ParkingSpotStatus> statuses,
			Collection<VehicleType> vehicleTypes) {
		String floorsStr = ParkingSpotQueryHelper.floorsListToString(floors);
		String[] statusList = ParkingSpotQueryHelper.statusList(statuses);
		String[] typeList = ParkingSpotQueryHelper
				.vehicleTypeList(vehicleTypes);

		return queryUnbookedExact(floorsStr, statusList, typeList);
	}

	/**
	 * Query number of parking spots without active bookings.
	 * 
	 * @param floors
	 *            The floors to look on, null or empty to query all.
	 * @param statuses
	 *            The statuses to query, null or empty to query all.
	 * @param vehicleTypes
	 *            The vehicle sizes to query, null or empty to query all.
	 * @return Number of queried results.
	 */
	default int queryUnbookedCount(char[] floors,
			Collection<ParkingSpotStatus> statuses,
			Collection<VehicleType> vehicleTypes) {
		String floorsStr = ParkingSpotQueryHelper.floorsListToString(floors);
		String[] statusList = ParkingSpotQueryHelper.statusList(statuses);
		String[] typeList = ParkingSpotQueryHelper
				.vehicleTypeList(vehicleTypes);

		return queryUnbookedExactCount(floorsStr, statusList, typeList);
	}

}
