package kr.ac.dankook.group5.azit.schedule;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class MergeIntervalsTimeRangeCompactor implements TimeRangeCompactor {
	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하는 시간대를 출력
	 * 
	 * @param schedules 일정
	 * @return 병합된 일정
	 */
	public List<TimeRange> combineOccupiedSchedules(List<TimeRange> sortedSchedules) {
		List<TimeRange> occupiedSchedules = new ArrayList<>();
		TimeRange activeSchedule = null;

		for (var currentSchedule : sortedSchedules) {
			if (activeSchedule == null) {
				activeSchedule = currentSchedule;
			} else if (activeSchedule.getEndTime().isBefore(currentSchedule.getStartTime())) {
				occupiedSchedules.add(activeSchedule);
				activeSchedule = currentSchedule;
			} else if (activeSchedule.getEndTime().isBefore(currentSchedule.getEndTime())) {
				activeSchedule = activeSchedule.withEndTime(currentSchedule.getEndTime());
			} else if (!activeSchedule.contains(currentSchedule)) {
				throw new IllegalArgumentException("Schedules must be sorted");
			}
		}

		if (activeSchedule != null) {
			occupiedSchedules.add(activeSchedule);
		}

		return occupiedSchedules;
	}

	public List<TimeRange> combineOccupiedSchedules(Set<TimeRange> schedules) {
		return combineOccupiedSchedules(timeRangeSetToSortedList(schedules));
	}

	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하지 않는 시간대를 출력
	 * 
	 * @param sortedSchedules 일정
	 * @return 일정이 존재하지 않는 시간대
	 */
	public List<TimeRange> findAvailableSchedules(List<TimeRange> sortedSchedules) {
		List<TimeRange> occupiedSchedules = combineOccupiedSchedules(sortedSchedules);
		List<TimeRange> availableSchedules = new ArrayList<>();

		occupiedSchedules.sort(Comparator.comparing(TimeRange::getStartTime));

		if (!occupiedSchedules.isEmpty()) {
			TimeRange first = occupiedSchedules.getFirst();
			if (LocalTime.MIN.isBefore(first.getStartTime())) {
				availableSchedules.add(new TimeRange(LocalTime.MIN, first.getStartTime()));
			}
		} else {
			availableSchedules.add(new TimeRange(LocalTime.MIN, LocalTime.MAX));
			return availableSchedules;
		}

		for (int i = 0; i < occupiedSchedules.size() - 1; i++) {
			TimeRange prev = occupiedSchedules.get(i);
			TimeRange next = occupiedSchedules.get(i + 1);

			availableSchedules.add(new TimeRange(prev.getEndTime(), next.getStartTime()));
		}

		{
			TimeRange last = occupiedSchedules.getLast();
			if (LocalTime.MAX.isAfter(last.getEndTime())) {
				availableSchedules.add(new TimeRange(last.getEndTime(), LocalTime.MAX));
			}
		}

		return availableSchedules;
	}

	public List<TimeRange> findAvailableSchedules(Set<TimeRange> schedules) {
		return findAvailableSchedules(timeRangeSetToSortedList(schedules));
	}

	private List<TimeRange> timeRangeSetToSortedList(Set<TimeRange> unsorted) {
		List<TimeRange> sorted = new ArrayList<>(unsorted);
		sorted.sort(Comparator.comparing(TimeRange::getStartTime));
		return sorted;
	}
}
