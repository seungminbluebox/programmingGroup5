package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Value;
import lombok.With;

@Value
@With
public class ScheduleSaveRequest {
	private String name;

	private LocalDate date;

	private LocalTime startTime;
	private LocalTime endTime;
}
