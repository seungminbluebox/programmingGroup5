package kr.ac.dankook.group5.azit.auth;

import kr.ac.dankook.group5.azit.project.ProjectJoinRequestService;
import kr.ac.dankook.group5.azit.project.ProjectRecommendationService;
import kr.ac.dankook.group5.azit.project.ProjectService;
import kr.ac.dankook.group5.azit.project.ProjectTaskRepository;
import kr.ac.dankook.group5.azit.schedule.repository.ScheduleRepository;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import kr.ac.dankook.group5.azit.user.MemberStack;
import kr.ac.dankook.group5.azit.user.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import kr.ac.dankook.group5.azit.user.Member;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberRepository memberRepository;
    private final ProjectService projectService;
    private final ProjectTaskRepository projectTaskRepository;
    private final ScheduleRepository scheduleRepository;
    private final TechStackRepository techStackRepository;
    private final ProjectRecommendationService projectRecommendationService;
    private final ProjectJoinRequestService projectJoinRequestService;

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }

        return "redirect:/discover";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (!isLoggedIn(authentication)) {
            return "redirect:/discover";
        }

        String email = authentication.getName();

        model.addAttribute("allStacks", techStackRepository.findAll());

        memberRepository.findByEmail(email).ifPresent(member -> {
            LocalDate today = LocalDate.now();

            model.addAttribute("nickname", member.getName());
            model.addAttribute("projects", projectService.findProjectsForMember(email));
            model.addAttribute("pendingInvitations", projectService.getPendingInvitations(email));

            model.addAttribute(
                    "todayTasks",
                    projectTaskRepository.findTop5ByAssigneeAndCompletedFalseOrderByIdDesc(member));

            model.addAttribute(
                    "upcomingSchedules",
                    scheduleRepository.findByMemberAndDateBetween(member, today, today.plusDays(7))
                            .stream()
                            .sorted(
                                    Comparator
                                            .comparing((
                                                    kr.ac.dankook.group5.azit.schedule.entity.Schedule schedule) -> schedule
                                                            .getDate())
                                            .thenComparing(schedule -> schedule.getStartTime()))
                            .limit(5)
                            .toList());
        });

        return "home";
    }

    @GetMapping("/discover")
    public String discover(Authentication authentication, Model model) {
        boolean loggedIn = isLoggedIn(authentication);
		String email = loggedIn ? authentication.getName() : null;

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("mainProjects", projectRecommendationService.findMainProjects(email));
		model.addAttribute("pendingInvitations", loggedIn ? projectService.getPendingInvitations(email) : List.of());

		memberRepository.findByEmail(email)
				.ifPresent(member -> {
					model.addAttribute("memberStacks",
							member.getMemberStacks().stream()
									.map(MemberStack::getTechStack)
									.toList());
				});

        return "project_explore";
    }

    @PostMapping("/discover/projects/{projectId}/join-requests")
    public String apply(
            Authentication authentication,
            @PathVariable Long projectId,
            RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(authentication)) {
            return "redirect:/login";
        }

        try {
            projectJoinRequestService.apply(authentication.getName(), projectId);
            redirectAttributes.addFlashAttribute("successMessage", "가입신청이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/discover";
    }

    @GetMapping("/projects/new")
    public String newProject(Authentication authentication, Model model) {
        if (!isLoggedIn(authentication)) {
            return "redirect:/login";
        }

        model.addAttribute("allStacks", techStackRepository.findAll());

        return "project_new";
    }

    @PostMapping("/join-requests/{requestId}/accept")
    public String acceptJoinRequest(
            Authentication authentication,
            @PathVariable Long requestId,
            RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(authentication)) {
            return "redirect:/login";
        }

        try {
            projectJoinRequestService.accept(authentication.getName(), requestId);
            redirectAttributes.addFlashAttribute("successMessage", "가입신청을 수락했습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/join-requests/{requestId}/reject")
    public String rejectJoinRequest(
            Authentication authentication,
            @PathVariable Long requestId,
            RedirectAttributes redirectAttributes) {
        if (!isLoggedIn(authentication)) {
            return "redirect:/login";
        }

        try {
            projectJoinRequestService.reject(authentication.getName(), requestId);
            redirectAttributes.addFlashAttribute("successMessage", "가입신청을 거절했습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}