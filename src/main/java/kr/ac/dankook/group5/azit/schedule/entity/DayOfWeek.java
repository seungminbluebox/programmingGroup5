package kr.ac.dankook.group5.azit.schedule.entity;

public enum DayOfWeek {
	MON("월요일"), TUE("화요일"), WED("수요일"), THU("목요일"), FRI("금요일"), SAT("토요일"), SUN("일요일");

	private final String label;

	DayOfWeek(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static DayOfWeek fromBuiltin(java.time.DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case java.time.DayOfWeek.MONDAY -> DayOfWeek.MON;
			case java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUE;
			case java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WED;
			case java.time.DayOfWeek.THURSDAY -> DayOfWeek.THU;
			case java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRI;
			case java.time.DayOfWeek.SATURDAY -> DayOfWeek.SAT;
			case java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUN;
		};
	}

	public java.time.DayOfWeek toBuiltin() {
		return switch (this) {
			case DayOfWeek.MON -> java.time.DayOfWeek.MONDAY;
			case DayOfWeek.TUE -> java.time.DayOfWeek.TUESDAY;
			case DayOfWeek.WED -> java.time.DayOfWeek.WEDNESDAY;
			case DayOfWeek.THU -> java.time.DayOfWeek.THURSDAY;
			case DayOfWeek.FRI -> java.time.DayOfWeek.FRIDAY;
			case DayOfWeek.SAT -> java.time.DayOfWeek.SATURDAY;
			case DayOfWeek.SUN -> java.time.DayOfWeek.SUNDAY;
		};
	}
}