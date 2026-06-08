package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;
import kr.ac.dankook.group5.azit.schedule.entity.Routine;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class RoutineInfoResponse {
	private String id;

	private String name;

	private DayOfWeek dayOfWeek;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime startTime;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime endTime;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;

	public static RoutineInfoResponse from(Routine routine) {
		return new RoutineInfoResponse(
				"R" + routine.getId(),
				routine.getName(),
				routine.getDayOfWeek(),
				routine.getStartTime(),
				routine.getEndTime(),
				routine.getStartDate(),
				routine.getEndDate());
	}
}
