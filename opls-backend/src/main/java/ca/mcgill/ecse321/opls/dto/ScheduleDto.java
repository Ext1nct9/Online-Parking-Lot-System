package ca.mcgill.ecse321.opls.dto;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonFormat;

import ca.mcgill.ecse321.opls.DateHelper;
import ca.mcgill.ecse321.opls.model.Schedule;
import ca.mcgill.ecse321.opls.model.Schedule.Day;
import jakarta.validation.constraints.NotNull;

/**
 * Model for a schedule entry.
 */
public class ScheduleDto {

	/**
	 * The day of the schedule.
	 */
	@NotNull
	public Day day;

	/**
	 * The start time of the schedule entry.
	 */
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.TIME_FORMAT)
	public Time startTime;

	/**
	 * The end time of the schedule entry.
	 */
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateHelper.TIME_FORMAT)
	public Time endTime;

	/** Default constructor. */
	public ScheduleDto() {
	}

	/** Constructor from the database entity. */
	public ScheduleDto(Schedule schedule) {
		this.day = schedule.getDay();
		this.startTime = schedule.getStartTime();
		this.endTime = schedule.getEndTime();
	}

	/** Constructor from fields. */
	public ScheduleDto(Day day, Time startTime, Time endTime) {
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
