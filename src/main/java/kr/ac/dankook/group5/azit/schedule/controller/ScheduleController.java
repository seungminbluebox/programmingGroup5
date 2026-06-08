package kr.ac.dankook.group5.azit.schedule.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import kr.ac.dankook.group5.azit.schedule.dto.DailySchedule;
import kr.ac.dankook.group5.azit.schedule.dto.RoutineInfoResponse;
import kr.ac.dankook.group5.azit.schedule.dto.RoutineSaveRequest;
import kr.ac.dankook.group5.azit.schedule.dto.ScheduleInfoResponse;
import kr.ac.dankook.group5.azit.schedule.dto.ScheduleSaveRequest;
import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
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

	@GetMapping(value = "/schedule/S{id}", headers = "Accept=text/html")
	public String viewScheduleInfo(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id,
			@RequestHeader(name="Hx-Target", required=false) String hxSelect,
					@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			Model model) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		Schedule schedule = scheduleService.getSchedule(member, id);
		addScheduleDetailModel(model, ScheduleInfoResponse.from(schedule));
		if (hxSelect == null) {
			addSchedulePageModel(userDetails, date != null ? date : schedule.getDate(), model);
			return "schedule";
		}
		return "schedule :: #" + hxSelect;
	}

	@GetMapping(value = "/schedule/S{id}")
	@ResponseBody
	public ScheduleInfoResponse getScheduleInfo(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		return ScheduleInfoResponse.from(scheduleService.getSchedule(member, id));
	}

	@GetMapping(value = "/schedule/R{id}", headers = "Accept=text/html")
	public String viewRoutineInfo(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id,
			@RequestHeader(name="Hx-Target", required=false) String hxSelect,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			Model model) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		Routine routine = scheduleService.getRoutine(member, id);
		addRoutineDetailModel(model, RoutineInfoResponse.from(routine));
		if (hxSelect == null) {
			addSchedulePageModel(userDetails, date != null ? date : LocalDate.now(), model);
			return "schedule";
		}
		return "schedule :: #" + hxSelect;
	}

	@GetMapping("/schedule/R{id}")
	@ResponseBody
	public RoutineInfoResponse getRoutineInfo(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		return RoutineInfoResponse.from(scheduleService.getRoutine(member, id));
	}

	@PostMapping(value = "/schedule", params = "scheduleType=SCHEDULE", headers = "Accept=text/html")
	public String createSchedule(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute ScheduleSaveRequest request,
			Model model) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		Schedule schedule = scheduleService.createSchedule(member, request);
		addSchedulePageModel(userDetails, schedule.getDate(), model);
		addScheduleDetailModel(model, ScheduleInfoResponse.from(schedule));
		return "schedule";
	}

	@PostMapping(value = "/schedule", params = "scheduleType=ROUTINE", headers = "Accept=text/html")
	public String createRoutine(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute RoutineSaveRequest request,
			Model model) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		Routine routine = scheduleService.createRoutine(member, request);
		addSchedulePageModel(userDetails, request.getStartDate(), model);
		addRoutineDetailModel(model, RoutineInfoResponse.from(routine));
		return "schedule";
	}

	@PostMapping(value = "/schedule", params = "scheduleType=SCHEDULE")
	@ResponseBody
	public ScheduleInfoResponse createScheduleJson(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute ScheduleSaveRequest request) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		return ScheduleInfoResponse.from(scheduleService.createSchedule(member, request));
	}

	@PostMapping(value = "/schedule", params = "scheduleType=ROUTINE")
	@ResponseBody
	public RoutineInfoResponse createRoutineJson(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute RoutineSaveRequest request) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		return RoutineInfoResponse.from(scheduleService.createRoutine(member, request));
	}

	@PutMapping(value = "/schedule/S{id}", headers = "Accept=text/html")
	public String updateSchedule(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id,
			@ModelAttribute ScheduleSaveRequest request,
			Model model) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		Schedule schedule = scheduleService.updateSchedule(member, id, request);
		addSchedulePageModel(userDetails, schedule.getDate(), model);
		addScheduleDetailModel(model, ScheduleInfoResponse.from(schedule));
		return "schedule";
	}

	@PutMapping(value = "/schedule/R{id}", headers = "Accept=text/html")
	public String updateRoutine(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id,
			@ModelAttribute RoutineSaveRequest request,
			Model model) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		Routine routine = scheduleService.updateRoutine(member, id, request);
		addSchedulePageModel(userDetails, routine.getStartDate(), model);
		addRoutineDetailModel(model, RoutineInfoResponse.from(routine));
		return "schedule";
	}

	@DeleteMapping("/schedule/S{id}")
	@ResponseStatus(HttpStatus.SEE_OTHER)
	public String deleteSchedule(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		scheduleService.deleteSchedule(member, id);
		return "redirect:/schedule";
	}

	@DeleteMapping("/schedule/R{id}")
	@ResponseStatus(HttpStatus.SEE_OTHER)
	public String deleteRoutine(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable long id) {
		Member member = profileService.getMemberByEmail(userDetails.getUsername());
		scheduleService.deleteRoutine(member, id);
		return "redirect:/schedule";
	}

	private void addScheduleDetailModel(Model model, ScheduleInfoResponse schedule) {
		model.addAttribute("selectedSchedule", schedule);
		model.addAttribute("selectedScheduleType", "SCHEDULE");
		model.addAttribute("selectedScheduleTypeLabel", "개인 일정");
		model.addAttribute("selectedScheduleEditLabel", "개인 일정 수정");
		model.addAttribute("selectedScheduleId", schedule.getId());
	}

	private void addRoutineDetailModel(Model model, RoutineInfoResponse routine) {
		model.addAttribute("selectedSchedule", routine);
		model.addAttribute("selectedScheduleType", "ROUTINE");
		model.addAttribute("selectedScheduleTypeLabel", "개인 반복 일정");
		model.addAttribute("selectedScheduleEditLabel", "개인 반복 일정 수정");
		model.addAttribute("selectedScheduleId", routine.getId());
	}

}
