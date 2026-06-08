package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class RoutineSaveRequest {
	private String name;

	private DayOfWeek dayOfWeek;

	private LocalTime startTime;
	private LocalTime endTime;

	private LocalDate startDate;
	private LocalDate endDate;

	public RoutineSaveRequest(String name, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
		this(name, dayOfWeek, startTime, endTime, null, null);
	}
}
