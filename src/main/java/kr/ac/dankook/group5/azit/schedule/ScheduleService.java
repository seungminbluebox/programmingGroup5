package kr.ac.dankook.group5.azit.schedule;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import kr.ac.dankook.group5.azit.user.Member;

@Service
public class ScheduleService {
	RoutineRepository routineRepository;
	ScheduleRepository scheduleRepository;
	TimeRangeCompactor timeRangeCompactor;

	public List<TimeRange> getMyAvailableTimeOnDate(Member member, LocalDate date) {
		Set<TimeRange> occupied = new HashSet<>();

		occupied.addAll(routineRepository.findByMemberAndDate(member, date, DayOfWeek.fromBuiltin(date.getDayOfWeek()),
				TimeRange.class));
		occupied.addAll(scheduleRepository.findByMemberAndDate(member, date, TimeRange.class));

		return timeRangeCompactor.findAvailableSchedules(occupied);
	}

	public List<TimeRange> getGroupAvailableTimeOnDate(Collection<Member> members, LocalDate date) {
		Set<TimeRange> occupied = new HashSet<>();

		occupied.addAll(routineRepository.findByMemberInAndDate(members, date,
				DayOfWeek.fromBuiltin(date.getDayOfWeek()), TimeRange.class));
		occupied.addAll(scheduleRepository.findByMemberInAndDate(members, date, TimeRange.class));

		return timeRangeCompactor.findAvailableSchedules(occupied);
	}

	public Map<DayOfWeek, List<TimeRange>> getMyAvailableTimeOnDateBetween(Member member, LocalDate startDate,
			LocalDate endDate) {
		Map<DayOfWeek, Set<TimeRange>> routines = routineRepository
				.findByMemberAndDateBetween(member, startDate, endDate)
				.stream()
				.collect(Collectors.groupingBy(Routine::getDayOfWeek,
						Collectors.mapping(Routine::toTimeRange, Collectors.toSet())));
		Map<DayOfWeek, Set<TimeRange>> schedules = scheduleRepository
				.findByMemberAndDateBetween(member, startDate, endDate).stream()
				.collect(
						Collectors.groupingBy(
								(Schedule schedule) -> DayOfWeek.fromBuiltin(schedule.getDate().getDayOfWeek()),
								Collectors.mapping(Schedule::toTimeRange, Collectors.toSet())));

		Map<DayOfWeek, Set<TimeRange>> occupied = Stream
				.concat(routines.entrySet().stream(), schedules.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(a, b) -> {
							var c = new HashSet<>(a);
							c.addAll(b);
							return c;
						}));

		return occupied.entrySet().stream().collect(
				Collectors.toMap(Map.Entry::getKey,
						entry -> timeRangeCompactor.findAvailableSchedules(entry.getValue())));
	}

	public Map<DayOfWeek, List<TimeRange>> getGroupAvailableTimeOnDate(Collection<Member> members, LocalDate startDate,
			LocalDate endDate) {
		Map<DayOfWeek, Set<TimeRange>> routines = routineRepository
				.findByMemberInAndDateBetween(members, startDate, endDate)
				.stream()
				.collect(Collectors.groupingBy(
						Routine::getDayOfWeek,
						() -> new EnumMap<>(DayOfWeek.class),
						Collectors.mapping(Routine::toTimeRange, Collectors.toSet())));
		Map<DayOfWeek, Set<TimeRange>> schedules = scheduleRepository
				.findByMemberInAndDateBetween(members, startDate, endDate).stream()
				.collect(
						Collectors.groupingBy(
								(Schedule schedule) -> DayOfWeek.fromBuiltin(schedule.getDate().getDayOfWeek()),
								() -> new EnumMap<>(DayOfWeek.class),
								Collectors.mapping(Schedule::toTimeRange, Collectors.toSet())));

		Map<DayOfWeek, Set<TimeRange>> occupied = Stream
				.concat(routines.entrySet().stream(), schedules.entrySet().stream())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(a, b) -> {
							var c = new HashSet<>(a);
							c.addAll(b);
							return c;
						},
						() -> new EnumMap<>(DayOfWeek.class)));

		return occupied.entrySet().stream().collect(
				Collectors.toMap(
						Map.Entry::getKey,
						entry -> timeRangeCompactor.findAvailableSchedules(entry.getValue()),
								(a, b) -> a,
						() -> new EnumMap<>(DayOfWeek.class)));
	}

}
