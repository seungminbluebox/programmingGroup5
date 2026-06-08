package kr.ac.dankook.group5.azit.schedule.dto;

import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.schedule.entity.Schedule;

public enum ScheduleType {
	SCHEDULE, ROUTINE;

	public String getPrefix() {
		return switch (this) {
			case ScheduleType.SCHEDULE -> "S";
			case ScheduleType.ROUTINE -> "R";
		};
	}

	public static ScheduleType getType(Schedule obj) {
		return ScheduleType.SCHEDULE;
	}

	public static ScheduleType getType(Routine obj) {
		return ScheduleType.ROUTINE;
	}
}
