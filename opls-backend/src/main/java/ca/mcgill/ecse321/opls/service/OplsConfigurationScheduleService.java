package ca.mcgill.ecse321.opls.service;

import ca.mcgill.ecse321.opls.exception.OplsApiException;
import ca.mcgill.ecse321.opls.model.ParkingLotSystem.ParkingLotSystemSchedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Service
public class OplsConfigurationScheduleService {

    @Autowired
    ParkingLotSystemRepository parkingLotSystemRepository;

    @Autowired
    ParkingLotSystemScheduleRepository parkingLotSystemScheduleRepository;

    /**
     * Retrieves the parking lot system schedule for the given day.
     * If no schedule for the given day exists, throws an API exception.
     * @param day           day of the parking lot system schedule to return
     * @return              parking lot system schedule for the given day
     */
    @Transactional
    public ParkingLotSystemSchedule getParkingLotSystemSchedule(Day day) {
        ParkingLotSystemSchedule plss = parkingLotSystemScheduleRepository.findActiveParkingLotScheduleByDay(day);

        if (isNull(plss)) {
            throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                    "a schedule for this day does not exist!");
        }

        return plss;
    }

    /**
     * Returns a list of all available parking lot system schedules
     * @return              list of all schedules
     */
    @Transactional
    public List<ParkingLotSystemSchedule> getAllParkingLotSystemSchedules() {
        ArrayList<ParkingLotSystemSchedule> schedules = new ArrayList<>();

        for (ParkingLotSystemSchedule plss: parkingLotSystemScheduleRepository.findActiveParkingLotSchedules()) {
            schedules.add(plss);
        }

        return schedules;
    }

    /**
     * Creates a new parking lot system schedule for the current active configuration for the given day, with the
     * given start and end times, and returns the newly created schedule.
     * If there already exists a schedule for the given day, or if the start time is after the end time,
     * throws an API exception.
     * @param day           day to create a parking lot system schedule for
     * @param startTime     start time of the schedule
     * @param endTime       end time of the schedule
     * @return              newly created parking lot system schedule
     */
    @Transactional
    public ParkingLotSystemSchedule createParkingLotSystemSchedule(Day day, Time startTime, Time endTime) {
        if (!isNull(parkingLotSystemScheduleRepository.findActiveParkingLotScheduleByDay(day))) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "a parking lot system configuration schedule for this day already exists!");
        }

        if (startTime.after(endTime)) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "the start time is after the end time!");
        }

        return parkingLotSystemScheduleRepository.save(
                parkingLotSystemRepository.getActiveParkingLotSystem().addSchedule(day, startTime, endTime));
    }

    /**
     * Updates the parking lot system schedule for the given day, with the given start and end times, and returns
     * the newly updated schedule.
     * If the start time is after the end time, throws an API exception.
     * If no schedule for the given day exists, creates a schedule with the given parameters.
     * @param day           day of the parking lot system schedule to update
     * @param startTime     new start time of the schedule
     * @param endTime       new end time of the schedule
     * @return              newly updated parking lot system schedule
     */
    @Transactional
    public ParkingLotSystemSchedule updateParkingLotSystemSchedule(Day day, Time startTime, Time endTime) {
        if (startTime.after(endTime)) {
            throw new OplsApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "the start time is after the end time!");
        }

        ParkingLotSystemSchedule plss = parkingLotSystemScheduleRepository.findActiveParkingLotScheduleByDay(day);

        if (isNull(plss)) {
            return parkingLotSystemScheduleRepository.save(parkingLotSystemRepository.getActiveParkingLotSystem().
                    addSchedule(day, startTime, endTime));
        }

        plss.setStartTime(startTime);
        plss.setEndTime(endTime);
        return parkingLotSystemScheduleRepository.save(plss);
    }

    /**
     * Deletes the parking lot system schedule for the given day.
     * If no schedule for the given day exists, throws an API exception.
     * @param day           day of the parking lot system schedule to delete
     */
    @Transactional
    public ParkingLotSystemSchedule deleteParkingLotSystemSchedule(Day day) {
        ParkingLotSystemSchedule plss = parkingLotSystemScheduleRepository.findActiveParkingLotScheduleByDay(day);

        if (isNull(plss)) {
            throw new OplsApiException(HttpStatus.NOT_FOUND, "not_found",
                    "a schedule for this day does not exist!");
        }

        parkingLotSystemScheduleRepository.delete(plss);
        return plss;
    }
}
