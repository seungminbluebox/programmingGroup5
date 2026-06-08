package kr.ac.dankook.group5.azit.schedule.dto;

import java.time.LocalTime;

import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class DailySchedule {
	private String name;
	private LocalTime startTime;
	private LocalTime endTime;

	private String id;

	public DailySchedule(String name, LocalTime startTime, LocalTime endTime) {
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.id = "";
	}

	public DailySchedule(String name, LocalTime startTime, LocalTime endTime, ScheduleType type, long id) {
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.id = type.getPrefix() + id;
	}

	public TimeRange toTimeRange() {
		return new TimeRange(startTime, endTime);
	}

	public static DailySchedule from(Schedule schedule) {
		return new DailySchedule(schedule.getName(), schedule.getStartTime(), schedule.getEndTime(),
				"S" + schedule.getId());
	}

	public static DailySchedule from(Routine routine) {
		return new DailySchedule(routine.getName(), routine.getStartTime(), routine.getEndTime(),
				"R" + routine.getId());
	}
}
