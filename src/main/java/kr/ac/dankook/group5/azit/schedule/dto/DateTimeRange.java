package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
import lombok.Value;
import lombok.With;

@Value
@With
public class DateTimeRange {
	private LocalTime startTime;
	private LocalTime endTime;
	private LocalDate date;

	public TimeRange toTimeRange() {
		return new TimeRange(startTime, endTime);
	}

	public static DateTimeRange from(TimeRange timeRange, LocalDate date) {
		return new DateTimeRange(timeRange.getStartTime(), timeRange.getEndTime(), date);
	}

	public static DateTimeRange from(Schedule schedule) {
		return new DateTimeRange(schedule.getStartTime(), schedule.getEndTime(), schedule.getDate());
	}

	public static DateTimeRange from(Routine routine, LocalDate date) {
		return new DateTimeRange(routine.getStartTime(), routine.getEndTime(), date);
	}
}
