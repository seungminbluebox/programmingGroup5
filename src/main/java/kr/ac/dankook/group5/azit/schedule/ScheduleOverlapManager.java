package kr.ac.dankook.group5.azit.schedule;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ScheduleOverlapManager {
	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하는 시간대를 출력
	 * 
	 * Schedule.dayOfWeek가 반드시 동일해야 함
	 * 
	 * @param schedules 일정
	 * @return 병합된 일정
	 */
	static public List<Schedule> combineOccupiedSchedules(Set<Schedule> schedules) {
		List<Schedule> sortedSchedules = new ArrayList<>(schedules);
		List<Schedule> occupiedSchedules = new ArrayList<>();
		Schedule activeSchedule = null;

		sortedSchedules.sort(Comparator.comparing(Schedule::getStartTime));

		for (var currentSchedule : sortedSchedules) {
			if (activeSchedule == null) {
				activeSchedule = currentSchedule;
			} else {
				if (activeSchedule.getEndTime().isBefore(currentSchedule.getStartTime())) {
					occupiedSchedules.add(activeSchedule);
					activeSchedule = currentSchedule;
				} else if (activeSchedule.getEndTime().isBefore(currentSchedule.getEndTime())) {
					activeSchedule = activeSchedule.withEndTime(currentSchedule.getEndTime());
				}
			}
		}

		if (activeSchedule != null) {
			occupiedSchedules.add(activeSchedule);
		}

		return occupiedSchedules;
	}

	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하지 않는 시간대를 출력
	 * 
	 * Schedule.dayOfWeek가 반드시 동일해야 함
	 * 
	 * @param schedules 일정
	 * @return 일정이 존재하지 않는 시간대
	 */
	static public List<Schedule> findAvailableSchedules(Set<Schedule> schedules) {
		List<Schedule> occupiedSchedules = combineOccupiedSchedules(schedules);
		List<Schedule> availableSchedules = new ArrayList<>();

		occupiedSchedules.sort(Comparator.comparing(Schedule::getStartTime));

		if (!occupiedSchedules.isEmpty()) {
			Schedule first = occupiedSchedules.getFirst();
			if (LocalTime.MIN.isBefore(first.getStartTime())) {
				availableSchedules.add(new Schedule(first.getDayOfWeek(), LocalTime.MIN, first.getStartTime()));
			}
		} else {
			availableSchedules.add(new Schedule(null, LocalTime.MIN, LocalTime.MAX));
			return availableSchedules;
		}

		for (int i = 0; i < occupiedSchedules.size() - 1; i++) {
			Schedule prev = occupiedSchedules.get(i);
			Schedule next = occupiedSchedules.get(i + 1);

			availableSchedules.add(new Schedule(prev.getDayOfWeek(), prev.getEndTime(), next.getStartTime()));
		}

		{
			Schedule last = occupiedSchedules.getLast();
			if (LocalTime.MAX.isAfter(last.getEndTime())) {
				availableSchedules.add(new Schedule(last.getDayOfWeek(), last.getEndTime(), LocalTime.MAX));
			}
		}

		return availableSchedules;
	}
}
