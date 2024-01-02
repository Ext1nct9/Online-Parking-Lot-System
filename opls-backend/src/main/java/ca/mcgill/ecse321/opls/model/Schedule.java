package ca.mcgill.ecse321.opls.model;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Model for a schedule interval on a single day of the week.
 */
@Entity
@Inheritance(
    strategy = InheritanceType.TABLE_PER_CLASS
)
public abstract class Schedule {
	
	/** Day of the week. */
	public static enum Day {
		SUNDAY(1), MONDAY(2), TUESDAY(3), WEDNESDAY(4), THURSDAY(5), FRIDAY(6), SATURDAY(7);
		
		private int dayCode;
		
		Day(int dayCode) {
			this.dayCode = dayCode;
		}
		
		/** Get the Day enum from Calendar.get(Calendar.DAY_OF_WEEK). */
		public static Day fromCalendarDay(int dayOfWeek) {
			for (var day : Day.values()) {
				if (day.dayCode == dayOfWeek) {
					return day;
				}
			}
			
			throw new IllegalArgumentException();
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Day day;
	
	@Temporal(TemporalType.TIME)
	private Time startTime;
	
	@Temporal(TemporalType.TIME)
	private Time endTime;
	
	public Integer getId() {
		return id;
	}

	public Day getDay() {
		return day;
	}

	public void setDay(Day day) {
		this.day = day;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}
	
	public boolean inScheduleOnDay(Date startDate, Date endDate) {
		var startTime = startDate.getTime();
		var endTime = endDate.getTime();
		
		// format opening schedule to milliseconds
		var scheduleStartTime = Schedule.getTimeOnDate(startDate, this.startTime);
		var scheduleEndTime = Schedule.getTimeOnDate(endDate, this.endTime);
		
		return (startTime < scheduleStartTime || endTime > scheduleEndTime);
	}
	
	public static long getTimeOnDate(Date date, Time time) {
		var localTime = time.toLocalTime();
		var calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		return calendar.getTimeInMillis();
	}

}
