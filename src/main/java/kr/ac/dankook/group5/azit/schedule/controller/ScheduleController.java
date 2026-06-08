package kr.ac.dankook.group5.azit.schedule.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.ac.dankook.group5.azit.schedule.dto.DailySchedule;
import kr.ac.dankook.group5.azit.schedule.dto.TimeRange;
import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;
import kr.ac.dankook.group5.azit.schedule.service.ScheduleService;
import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.ProfileService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ScheduleController {
	private static final int TIMETABLE_START_HOUR = 0;
	private static final int TIMETABLE_END_HOUR = 24;
	private static final int SLOTS_PER_HOUR = 6;

	private final ScheduleService scheduleService;
	private final ProfileService profileService;

	@GetMapping("/schedule")
	public String viewMySchedule(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			Model model) {
		addSchedulePageModel(userDetails, date, model);
		return "schedule";
	}

	private void addSchedulePageModel(UserDetails userDetails, LocalDate date, Model model) {
		LocalDate weekSelector = date != null ? date : LocalDate.now();
		LocalDate weekStart = weekSelector.minusDays(weekSelector.getDayOfWeek().getValue() % 7L);
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		List<List<DailySchedule>> schedule = new ArrayList<>();

		for (int day = 0; day < 7; day++) {
			schedule.add(scheduleService.getMyDailySchedule(member, weekStart.plusDays(day)));
		}

		List<Integer> timeLabels = new ArrayList<>();
		for (int hour = TIMETABLE_START_HOUR; hour < TIMETABLE_END_HOUR; hour++) {
			timeLabels.add(hour);
		}

		model.addAttribute("schedule", schedule);
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("weekStart", weekStart);
		model.addAttribute("timeLabels", timeLabels);
		model.addAttribute("timetableStartHour", TIMETABLE_START_HOUR);
		model.addAttribute("timetableSlotCount", (TIMETABLE_END_HOUR - TIMETABLE_START_HOUR) * SLOTS_PER_HOUR);
		model.addAttribute("timetableRowEnd", 2 + (TIMETABLE_END_HOUR - TIMETABLE_START_HOUR) * SLOTS_PER_HOUR);
	}

	@GetMapping("/schedule/S{id}")
	public String viewScheduleInfo(@PathVariable("id") long id) {

	}

	@GetMapping("/scheudle/R{id}")
	public String viewRoutineInfo(@PathVariable("id") long id) {

	}

	@PostMapping(value = "/schedule", params = "scheduleType=SCHEDULE")
	public String createSchedule(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam String scheduleType,
			@RequestParam String name,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
			RedirectAttributes redirectAttributes) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		TimeRange timeRange = new TimeRange(startTime, endTime);
		scheduleService.createSchedule(member, name, scheduleDate, timeRange);
		redirectAttributes.addAttribute("date", scheduleDate.toString());
		return "redirect:/schedule";
	}

	@PostMapping(value = "/schedule", params = "scheduleType=ROUTINE")
	public String createRoutine(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam String scheduleType,
			@RequestParam String name,
			@RequestParam DayOfWeek dayOfWeek,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
			RedirectAttributes redirectAttributes) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		TimeRange timeRange = new TimeRange(startTime, endTime);
		scheduleService.createRoutine(member, name, dayOfWeek, timeRange, startDate, endDate);
		return "redirect:/schedule";
	}

	@PatchMapping("/schedule/S{id}")
	public String updateSchedule(@PathVariable("id") long id) {

	}

	@PatchMapping("/schedule/R{id}")
	public String updateRoutine(@PathVariable("id") long id) {

	}

}
