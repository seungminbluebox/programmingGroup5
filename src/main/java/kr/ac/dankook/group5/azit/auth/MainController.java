package kr.ac.dankook.group5.azit.auth;

import kr.ac.dankook.group5.azit.project.ProjectService;
import kr.ac.dankook.group5.azit.project.ProjectTaskRepository;
import kr.ac.dankook.group5.azit.schedule.repository.ScheduleRepository;
import kr.ac.dankook.group5.azit.user.MemberRepository;
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

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/main";
        }

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
        }
        return "home";
    }
}
