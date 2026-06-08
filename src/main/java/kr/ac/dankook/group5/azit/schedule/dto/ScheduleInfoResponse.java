package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class ScheduleInfoResponse {
	private String id;

	private String name;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate date;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime startTime;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime endTime;

	public static ScheduleInfoResponse from(Schedule schedule) {
		return new ScheduleInfoResponse(
				"S" + schedule.getId(),
				schedule.getName(),
				schedule.getDate(),
				schedule.getStartTime(),
				schedule.getEndTime());
	}
}
