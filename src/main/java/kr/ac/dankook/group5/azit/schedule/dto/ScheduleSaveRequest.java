package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
import kr.ac.dankook.group5.azit.user.Member;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class ScheduleSaveRequest {
	private String name;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate date;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime startTime;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime endTime;

	public Schedule toSchedule(Member member) {
		return Schedule.builder()
				.member(member)
				.name(name)
				.date(date)
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}
}
