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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import kr.ac.dankook.group5.azit.schedule.TimeRangeCompactor;
import kr.ac.dankook.group5.azit.schedule.dto.DailySchedule;
import kr.ac.dankook.group5.azit.schedule.dto.RoutineSaveRequest;
import kr.ac.dankook.group5.azit.schedule.dto.ScheduleSaveRequest;
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
@Transactional(readOnly = true)
public class ScheduleService {
	private final RoutineRepository routineRepository;
	private final ScheduleRepository scheduleRepository;
	private final TimeRangeCompactor timeRangeCompactor;

	public List<TimeRange> getMyAvailableTimeOnDate(Member member, LocalDate date) {
		Set<TimeRange> occupied = Stream.concat(
				routineRepository.findByMemberAndDate(member, date, DayOfWeek.fromBuiltin(date.getDayOfWeek()))
						.stream().map(Routine::toTimeRange),
				scheduleRepository.findByMemberAndDate(member, date)
						.stream().map(Schedule::toTimeRange))
				.collect(Collectors.toSet());

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

	public Schedule getSchedule(Member member, long id) {
		return scheduleRepository.findByIdAndMember(id, member)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	public Routine getRoutine(Member member, long id) {
		return routineRepository.findByIdAndMember(id, member)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@Transactional
	public void deleteSchedule(Member member, long id) {
		scheduleRepository.delete(getSchedule(member, id));
	}

	@Transactional
	public void deleteRoutine(Member member, long id) {
		routineRepository.delete(getRoutine(member, id));
	}

	@Transactional
	public Schedule createSchedule(Member member, ScheduleSaveRequest request) {
		Schedule schedule = request.toSchedule(member);
		return scheduleRepository.save(schedule);
	}

	@Transactional
	public Routine createRoutine(Member member, RoutineSaveRequest request) {
		Routine routine = request.toRoutine(member);
		return routineRepository.save(routine);
	}

	@Transactional
	public Schedule updateSchedule(Member member, long id, ScheduleSaveRequest request) {
		Schedule schedule = getSchedule(member, id);
		schedule.setName(request.getName());
		schedule.setDate(request.getDate());
		schedule.setStartTime(request.getStartTime());
		schedule.setEndTime(request.getEndTime());
		return scheduleRepository.save(schedule);
	}

	@Transactional
	public Routine updateRoutine(Member member, long id, RoutineSaveRequest request) {
		Routine routine = getRoutine(member, id);
		routine.setName(request.getName());
		routine.setDayOfWeek(request.getDayOfWeek());
		routine.setStartTime(request.getStartTime());
		routine.setEndTime(request.getEndTime());
		routine.setStartDate(request.getStartDate());
		routine.setEndDate(request.getEndDate());
		return routineRepository.save(routine);
	}

	public List<TimeRange> getGroupAvailableTimeOnDate(Collection<Member> members, LocalDate date) {
		Set<TimeRange> occupied = Stream.concat(
				routineRepository
						.findByMemberInAndDate(members, date, DayOfWeek.fromBuiltin(date.getDayOfWeek()), Routine.class)
						.stream().map(Routine::toTimeRange),
				scheduleRepository
						.findByMemberInAndDate(members, date, Schedule.class)
						.stream().map(Schedule::toTimeRange))
				.collect(Collectors.toSet());

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
