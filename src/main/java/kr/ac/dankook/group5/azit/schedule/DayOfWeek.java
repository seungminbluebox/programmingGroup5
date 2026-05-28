package kr.ac.dankook.group5.azit.schedule;

public enum DayOfWeek {
	MON, TUE, WED, THU, FRI, SAT, SUN;

	static DayOfWeek fromBuiltin(java.time.DayOfWeek dayOfWeek) {
		return values()[dayOfWeek.ordinal()];
	}
}