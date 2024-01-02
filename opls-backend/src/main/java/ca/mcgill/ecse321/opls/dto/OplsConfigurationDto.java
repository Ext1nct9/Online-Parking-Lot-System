package ca.mcgill.ecse321.opls.dto;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request and response model for the system configuration.
 */
public class OplsConfigurationDto {

    /**
     * Monthly fee of the configuration
     */
    @DecimalMin(value = "0.0", message = "The monthly fee must be a positive number greater than 0.0!")
    @NotNull
    public BigDecimal monthlyFee;

    /**
     * Increment fee of the configuration
     */
    @DecimalMin(value = "0.0", message = "The increment fee must be a positive number greater than 0.0!")
    @NotNull
    public BigDecimal incrementFee;

    /**
     * Increment time of the configuration
     */
    @Min(value = 0, message = "The increment time must be a positive integer greater than 0!")
    public int incrementTime;

    /**
     * Max increment time of the configuration
     */
    @Min(value = 0, message = "The max increment time must be a positive integer greater than 0!")
    public int maxIncrementTime;

    /**
     * Default constructor for deserialization
     */
    public OplsConfigurationDto() {}

    /** Constructor from fields. */
    public OplsConfigurationDto(Double monthlyFee, Double incrementFee, int incrementTime, int maxIncrementTime) {
        this.monthlyFee = BigDecimal.valueOf(monthlyFee);
        this.incrementFee = BigDecimal.valueOf(incrementFee);
        this.incrementTime = incrementTime;
        this.maxIncrementTime = maxIncrementTime;
    }

    /** Constructor from the database entity. */
    public OplsConfigurationDto(ParkingLotSystem pls) {
        this.monthlyFee = BigDecimal.valueOf(pls.getMonthlyFee());
        this.incrementFee = BigDecimal.valueOf(pls.getIncrementFee());
        this.incrementTime = pls.getIncrementTime();
        this.maxIncrementTime = pls.getMaxIncrementTime();
    }

    /**
     * Returns the monthly fee of this configuration as a double
     * @return      monthly fee of the configuration
     */
    public double getMonthlyFee() {
        return monthlyFee.doubleValue();
    }

    /**
     * Returns the increment fee of this configuration as a double
     * @return      increment fee of the configuration
     */
    public double getIncrementFee() {
        return incrementFee.doubleValue();
    }
   
}
