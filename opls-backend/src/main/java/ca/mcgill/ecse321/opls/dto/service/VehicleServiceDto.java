package ca.mcgill.ecse321.opls.dto.service;

import org.hibernate.validator.constraints.Length;

import ca.mcgill.ecse321.opls.model.VehicleService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Data transfer object for the Vehicle Service class, holding information about the service's display name
 */
public class VehicleServiceDto {
	
	/**
	 * The id of the vehicle service
	 */
	public String id;

	/**
     * Display name of the vehicle service
     */
    @Length(min = 1, message = "The field must be at least 1 character!")
    @NotNull
    public String displayName;
	
	/**
     * Vehicle service fee
     */
    @DecimalMin(value = "0.0", message = "The fee must be a positive number greater than 0.0!")
    @NotNull
    public double fee;
	
	/**
	 * The duration of the vehicle service
	 */
	@DecimalMin(value = "0.0", message = "The duration must be a positive number greater than 0.0!")
	@NotNull
	public int duration;

	
	/** 
	 * Default constructor for deserialization.
	 */
	public VehicleServiceDto() {}
	
	/** 
	 * Constructor from the internal model. 
	 */

	public VehicleServiceDto(VehicleService vs) {
		this.id = vs.getId();
		this.displayName = vs.getDisplayName();
		this.fee = vs.getFee();
		this.duration = vs.getDuration();
	}

	/** 
	 * Constructor from the internal model. 
	 */

	 public VehicleServiceDto(String displayName, double fee, int duration) {
		this.displayName = displayName;
		this.fee = fee;
		this.duration = duration;
	}
	
}
