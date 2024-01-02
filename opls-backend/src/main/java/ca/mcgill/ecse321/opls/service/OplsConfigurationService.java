package ca.mcgill.ecse321.opls.service;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OplsConfigurationService {

    @Autowired
    ParkingLotSystemRepository parkingLotSystemRepository;

    /**
     * Retrieves the current active parking lot system configuration from the parking lot system configuration
     * repository.
     * @return              the current active ParkingLotSystem configuration
     */
    @Transactional
    public ParkingLotSystem getParkingLotSystem() {
        return parkingLotSystemRepository.getActiveParkingLotSystem();
    }

    /**
     * Updates all the parameters of the current active parking lot system configuration
     * @param monthlyFee            new monthly fee
     * @param incrementFee          new increment fee
     * @param incrementTime         new increment time
     * @param maxIncrementTime      new max increment time
     */
    @Transactional
    public ParkingLotSystem updateParkingLotSystemConfig(
            double monthlyFee, double incrementFee, int incrementTime, int maxIncrementTime) {
        ParkingLotSystem pls = parkingLotSystemRepository.getActiveParkingLotSystem();
        pls.setMonthlyFee(monthlyFee);
        pls.setIncrementFee(incrementFee);
        pls.setIncrementTime(incrementTime);
        pls.setMaxIncrementTime(maxIncrementTime);
        return parkingLotSystemRepository.save(pls);
    }

    /**
     * Updates the monthly fee field of the current active parking lot system configuration.
     * If the fee is negative, throws an API exception.
     * @param monthlyFee    new monthly fee
     */
    @Transactional
    public ParkingLotSystem setParkingLotSystemMonthlyFee(double monthlyFee) {
        ParkingLotSystem pls = parkingLotSystemRepository.getActiveParkingLotSystem();
        pls.setMonthlyFee(monthlyFee);
        return parkingLotSystemRepository.save(pls);
    }

    /**
     * Updates the increment fee field of the current active parking lot system configuration.
     * If the fee is negative, throws an API exception.
     * @param incrementFee    new increment fee
     */
    @Transactional
    public ParkingLotSystem setParkingLotSystemIncrementFee(double incrementFee) {
        ParkingLotSystem pls = parkingLotSystemRepository.getActiveParkingLotSystem();
        pls.setIncrementFee(incrementFee);
        return parkingLotSystemRepository.save(pls);
    }

    /**
     * Updates the increment time field of the current active parking lot system configuration.
     * If the increment time is negative, throws an API exception.
     * @param incrementTime     new increment time
     */
    @Transactional
    public ParkingLotSystem setParkingLotSystemIncrementTime(int incrementTime) {
        ParkingLotSystem pls = parkingLotSystemRepository.getActiveParkingLotSystem();
        pls.setIncrementTime(incrementTime);
        return parkingLotSystemRepository.save(pls);
    }

    /**
     * Updates the max increment time field of the current active parking lot system configuration
     * If the max increment time is negative, throws an API exception.
     * @param maxIncrementTime     new max increment time
     */
    @Transactional
    public ParkingLotSystem setParkingLotSystemMaxIncrementTime(int maxIncrementTime) {
        ParkingLotSystem pls = parkingLotSystemRepository.getActiveParkingLotSystem();
        pls.setMaxIncrementTime(maxIncrementTime);
        return parkingLotSystemRepository.save(pls);
    }

}
