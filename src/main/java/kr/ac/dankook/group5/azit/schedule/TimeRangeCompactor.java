package kr.ac.dankook.group5.azit.schedule;

import java.util.List;
import java.util.Set;

public interface TimeRangeCompactor {
	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하는 시간대를 출력
	 * 
	 * @param schedules 일정
	 * @return 병합된 일정
	 */
	List<TimeRange> combineOccupiedSchedules(Set<TimeRange> schedules);

	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하는 시간대를 출력
	 * 
	 * @param sortedSchedules 시작 시간으로 정렬된 일정
	 * @return 병합된 일정
	 */

	List<TimeRange> combineOccupiedSchedules(List<TimeRange> sortedSchedules);

	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하지 않는 시간대를 출력
	 * 
	 * @param schedules 일정
	 * @return 일정이 존재하지 않는 시간대
	 */
	List<TimeRange> findAvailableSchedules(Set<TimeRange> schedules);

	/**
	 * 하루의 여러 일정을 받고, 일정이 존재하지 않는 시간대를 출력
	 * 
	 * @param sortedSchedules 시작 시간으로 정렬된 일정
	 * @return 일정이 존재하지 않는 시간대
	 */
	List<TimeRange> findAvailableSchedules(List<TimeRange> sortedSchedules);
}
