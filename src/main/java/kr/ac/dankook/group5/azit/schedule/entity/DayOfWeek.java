package kr.ac.dankook.group5.azit.schedule.entity;

public enum DayOfWeek {
	MON, TUE, WED, THU, FRI, SAT, SUN;

	public static DayOfWeek fromBuiltin(java.time.DayOfWeek dayOfWeek) {
		return values()[dayOfWeek.ordinal()];
	}
}