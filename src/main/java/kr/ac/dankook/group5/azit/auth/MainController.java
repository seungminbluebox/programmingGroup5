package kr.ac.dankook.group5.azit.auth;

import kr.ac.dankook.group5.azit.project.ProjectRepository;
import kr.ac.dankook.group5.azit.project.ProjectService;
import kr.ac.dankook.group5.azit.project.ProjectTaskRepository;
import kr.ac.dankook.group5.azit.schedule.repository.ScheduleRepository;
import kr.ac.dankook.group5.azit.project.ProjectStatus;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import kr.ac.dankook.group5.azit.user.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Comparator;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final MemberRepository memberRepository;
	private final ProjectService projectService;
	private final ProjectTaskRepository projectTaskRepository;
	private final ScheduleRepository scheduleRepository;
	private final TechStackRepository techStackRepository;
	private final ProjectRepository projectRepository;

	@GetMapping("/projects/new")
	public String exploreProjects(Model model, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal().equals("anonymousUser")) {
			return "redirect:/login";
		}
		model.addAttribute("publicProjects",
				projectRepository.findAllByStatusOrderByCreatedAtDesc(ProjectStatus.IN_PROGRESS));
		return "project_explore";
	}

	@GetMapping("/projects/create")
	public String newProject(Model model, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal().equals("anonymousUser")) {
			return "redirect:/login";
		}
		model.addAttribute("allStacks", techStackRepository.findAll());
		return "project_new";
	}

	@GetMapping("/")
	public String home(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal().equals("anonymousUser")) {
			return "redirect:/main";
		}

		model.addAttribute("allStacks", techStackRepository.findAll());

		if (authentication != null && authentication.isAuthenticated()
				&& !authentication.getPrincipal().equals("anonymousUser")) {
			String email = authentication.getName();
			memberRepository.findByEmail(email).ifPresent(member -> {
				LocalDate today = LocalDate.now();

				model.addAttribute("nickname", member.getName());
				model.addAttribute("projects", projectService.findProjectsForMember(email));
				model.addAttribute("pendingInvitations",
						projectService.getPendingInvitations(authentication.getName()));

				model.addAttribute(
						"todayTasks",
						projectTaskRepository.findTop5ByAssigneeAndCompletedFalseOrderByIdDesc(
								member));

				model.addAttribute(
						"upcomingSchedules",
						scheduleRepository
								.findByMemberAndDateBetween(member, today,
										today.plusDays(7))
								.stream()
								.sorted(
										Comparator
												.comparing((
														kr.ac.dankook.group5.azit.schedule.entity.Schedule schedule) -> schedule
																.getDate())
												.thenComparing(schedule -> schedule
														.getStartTime()))
								.limit(5)
								.toList());
			});
		}
		return "home";
	}
}