package kr.ac.dankook.group5.azit.schedule.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import kr.ac.dankook.group5.azit.schedule.TimeRangeCompactor;
import kr.ac.dankook.group5.azit.schedule.dto.DailySchedule;
import kr.ac.dankook.group5.azit.schedule.dto.TimeRange;
import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;
import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
import kr.ac.dankook.group5.azit.schedule.repository.RoutineRepository;
import kr.ac.dankook.group5.azit.schedule.repository.ScheduleRepository;
import kr.ac.dankook.group5.azit.user.Member;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScheduleService {
	private final RoutineRepository routineRepository;
	private final ScheduleRepository scheduleRepository;
	private final TimeRangeCompactor timeRangeCompactor;

	public List<TimeRange> getMyAvailableTimeOnDate(Member member, LocalDate date) {
		Set<TimeRange> occupied = new HashSet<>();

		occupied.addAll(routineRepository.findByMemberAndDate(member, date, DayOfWeek.fromBuiltin(date.getDayOfWeek()),
				TimeRange.class));
		occupied.addAll(scheduleRepository.findByMemberAndDate(member, date, TimeRange.class));

		return timeRangeCompactor.findAvailableSchedules(occupied);
	}

	public List<DailySchedule> getMyDailySchedule(Member member, LocalDate date) {
		List<DailySchedule> schedules = new ArrayList<>();

		schedules.addAll(
				routineRepository.findByMemberAndDate(member, date, DayOfWeek.fromBuiltin(date.getDayOfWeek()))
						.stream()
						.map(DailySchedule::from)
						.toList());
		schedules.addAll(
				scheduleRepository.findByMemberAndDate(member, date)
						.stream()
						.map(DailySchedule::from)
						.toList());

		return schedules;
	}

	public Schedule createSchedule(Member member, String name, LocalDate date, TimeRange timeRange) {
		Schedule schedule = new Schedule();
		schedule.setMember(member);
		schedule.setName(name);
		schedule.setDate(date);
		schedule.setStartTime(timeRange.getStartTime());
		schedule.setEndTime(timeRange.getEndTime());
		return scheduleRepository.save(schedule);
	}

	public Routine createRoutine(
			Member member,
			String name,
			DayOfWeek dayOfWeek,
			TimeRange timeRange,
			LocalDate startDate,
			LocalDate endDate) {
		Routine routine = new Routine();
		routine.setMember(member);
		routine.setName(name);
		routine.setDayOfWeek(dayOfWeek);
		routine.setStartTime(timeRange.getStartTime());
		routine.setEndTime(timeRange.getEndTime());
		routine.setStartDate(startDate);
		routine.setEndDate(endDate);
		return routineRepository.save(routine);
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
