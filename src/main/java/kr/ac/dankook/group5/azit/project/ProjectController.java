package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.schedule.dto.DateTimeRange;
import kr.ac.dankook.group5.azit.schedule.dto.TimeRange;
import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;
import kr.ac.dankook.group5.azit.schedule.service.ScheduleService;
import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.project.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ScheduleService scheduleService;
    private final MatchingService matchingService;

    @PostMapping("/projects")
    public String createProject(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) List<Long> requiredStackIds,
            @RequestParam(value = "availDay", required = false) List<String> availDays,
            @RequestParam(value = "availStart", required = false) List<String> availStarts,
            @RequestParam(value = "availEnd", required = false) List<String> availEnds) {
        Project project = projectService.createProject(authentication.getName(), title, description);

        if (requiredStackIds != null && !requiredStackIds.isEmpty()) {
            projectService.addProjectRequiredStacks(project.getId(), requiredStackIds);
        }

        if (availDays != null && !availDays.isEmpty()) {
            projectService.addProjectAvailabilities(project.getId(), availDays, availStarts, availEnds);
        }

        return "redirect:/project/" + project.getId();
    }

    @GetMapping("/project/{projectId}")
    public String detail(
            Authentication authentication,
            @PathVariable Long projectId,
            Model model) {
        String view = detailPage(authentication, projectId, "overview", "project_detail", model);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7L);
        Project project = (Project) model.getAttribute("project");
        List<Member> allMembers = project.getMembers().stream()
                .map(ProjectMember::getMember).toList();

        Map<DayOfWeek, List<TimeRange>> availableMap = scheduleService.getGroupAvailableTimeOnDate(allMembers,
                weekStart, weekStart.plusDays(6));
        DayOfWeek[] dayOrder = { DayOfWeek.SUN, DayOfWeek.MON, DayOfWeek.TUE,
                DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI, DayOfWeek.SAT };
        List<List<TimeRange>> available = new ArrayList<>();
        for (DayOfWeek day : dayOrder) {
            available.add(availableMap.containsKey(day)
                    ? availableMap.get(day)
                    : List.of(new TimeRange(LocalTime.MIN, LocalTime.MAX)));
        }
        model.addAttribute("topAvailableSlots", computeTopSlots(available, weekStart));
        return view;
    }

    @GetMapping("/project/{projectId}/task")
    public String task(
            Authentication authentication,
            @PathVariable Long projectId,
            Model model) {
        return detailPage(authentication, projectId, "task", "project_task", model);
    }

    @GetMapping("/project/{projectId}/schedule")
    public String schedule(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<Long> memberIds,
            Model model) {
        LocalDate today = LocalDate.now();
        LocalDate base = date != null ? date : today;
        LocalDate weekStart = base.minusDays(base.getDayOfWeek().getValue() % 7L);

        detailPage(authentication, projectId, "schedule", "project_schedule", model);

        Project project = (Project) model.getAttribute("project");
        List<Member> allMembers = project.getMembers().stream()
                .map(ProjectMember::getMember)
                .toList();

        boolean filtered = memberIds != null && !memberIds.isEmpty();
        List<Member> members = filtered
                ? allMembers.stream().filter(m -> memberIds.contains(m.getId())).toList()
                : allMembers;

        Set<Long> selectedMemberIds = filtered
                ? new HashSet<>(memberIds)
                : allMembers.stream().map(Member::getId).collect(java.util.stream.Collectors.toSet());
        model.addAttribute("selectedMemberIds", selectedMemberIds);
        Map<DayOfWeek, List<TimeRange>> availableMap = scheduleService.getGroupAvailableTimeOnDate(members, weekStart,
                weekStart.plusDays(6));

        DayOfWeek[] dayOrder = { DayOfWeek.SUN, DayOfWeek.MON, DayOfWeek.TUE,
                DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI, DayOfWeek.SAT };
        List<List<TimeRange>> available = new ArrayList<>();
        for (DayOfWeek day : dayOrder) {
            available.add(availableMap.containsKey(day)
                    ? availableMap.get(day)
                    : List.of(new TimeRange(LocalTime.MIN, LocalTime.MAX)));
        }

        model.addAttribute("weekStart", weekStart);
        model.addAttribute("today", today);
        model.addAttribute("available", available);
        model.addAttribute("topAvailableSlots", computeTopSlots(available, weekStart));
        return "project_schedule";
    }

    private static List<DateTimeRange> computeTopSlots(List<List<TimeRange>> available, LocalDate weekStart) {
        List<DateTimeRange> all = new ArrayList<>();
        for (int i = 0; i < available.size(); i++) {
            LocalDate date = weekStart.plusDays(i);
            for (TimeRange r : available.get(i)) {
                if (java.time.temporal.ChronoUnit.MINUTES.between(r.getStartTime(), r.getEndTime()) < 30)
                    continue;
                all.add(new DateTimeRange(r.getStartTime(), r.getEndTime(), date));
            }
        }
        return all.stream()
                .sorted(Comparator.comparingLong((DateTimeRange r) -> java.time.temporal.ChronoUnit.MINUTES
                        .between(r.getStartTime(), r.getEndTime())).reversed())
                .limit(5)
                .toList();
    }

    @GetMapping("/project/{projectId}/member")
    public String member(
            Authentication authentication,
            @PathVariable Long projectId,
            Model model) {
        return detailPage(authentication, projectId, "member", "project_member", model);
    }

    @GetMapping("/project/{projectId}/settings")
    public String settings(
            Authentication authentication,
            @PathVariable Long projectId,
            Model model) {
        return detailPage(authentication, projectId, "settings", "project_settings", model);
    }

    private String detailPage(
            Authentication authentication,
            Long projectId,
            String activeProjectPage,
            String templateName,
            Model model) {
        String email = authentication.getName();

        Project project = projectService.getProjectForMember(email, projectId);
        List<ProjectTask> tasks = projectService.getTasks(email, projectId);

        model.addAttribute("project", project);
        model.addAttribute("projectMembers", projectService.getProjectMembers(email, projectId));
        model.addAttribute("recommendedMembers", matchingService.recommendMembers(projectId));
        model.addAttribute("tasks", tasks);
        model.addAttribute("myTaskCompletionRate", projectService.getMyTaskCompletionRate(email, projectId));
        model.addAttribute("myCompletedTaskCount", projectService.getMyCompletedTaskCount(email, projectId));
        model.addAttribute("inviteCandidates", projectService.getInviteCandidates(email, projectId));
        model.addAttribute("pendingInvitations", projectService.getPendingInvitations(email));
        model.addAttribute("projectOwner", projectService.isProjectOwner(email, projectId));
        model.addAttribute("projectStatuses", ProjectStatus.values());
        model.addAttribute("projectEditable", project.getStatus() == ProjectStatus.IN_PROGRESS);
        model.addAttribute("incompleteTaskCount", tasks.stream().filter(task -> !task.isCompleted()).count());
        model.addAttribute("activeProjectPage", activeProjectPage);
        model.addAttribute("deadlineProgressRate", projectService.getDeadlineProgressRate(email, projectId));
        model.addAttribute("teamTaskCompletionRate", projectService.getTeamTaskCompletionRate(email, projectId));
        model.addAttribute("deadlineDday", projectService.getDeadlineDday(email, projectId));

        return templateName;
    }

    @PostMapping("/project/{projectId}/links")
    public String addLink(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String label,
            @RequestParam String url,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.addProjectLink(authentication.getName(), projectId, label, url);
            redirectAttributes.addFlashAttribute("successMessage", "작업 링크가 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/settings";
    }

    @PostMapping("/project/{projectId}/status")
    public String updateStatus(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam ProjectStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.updateProjectStatus(authentication.getName(), projectId, status);
            redirectAttributes.addFlashAttribute("successMessage", "프로젝트 상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/settings";
    }

    @PostMapping("/project/{projectId}/members")
    public String sendInvitationByEmail(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String memberEmail,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.sendInvitationByEmail(authentication.getName(), projectId, memberEmail);
            redirectAttributes.addFlashAttribute("successMessage", "초대를 보냈습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/member";
    }

    @PostMapping("/project/{projectId}/tasks")
    public String addTask(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String title,
            @RequestParam Long assigneeId,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.addTask(authentication.getName(), projectId, title, assigneeId);
            redirectAttributes.addFlashAttribute("successMessage", "태스크가 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/task";
    }

    @PostMapping("/project/{projectId}/tasks/{taskId}/toggle")
    public String toggleTask(
            Authentication authentication,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.toggleTask(authentication.getName(), projectId, taskId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/task";
    }

    @PostMapping("/project/{projectId}/invitations")
    public String sendInvitation(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam Long receiverId,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.sendInvitation(authentication.getName(), projectId, receiverId);
            redirectAttributes.addFlashAttribute("successMessage", "초대를 보냈습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/member";
    }

    @GetMapping("/invitations")
    public String invitations(Authentication authentication, Model model) {
        model.addAttribute("pendingInvitations", projectService.getPendingInvitations(authentication.getName()));
        return "invitations";
    }

    @GetMapping("/invitations/{invitationId}")
    public String invitationDetail(
            Authentication authentication,
            @PathVariable Long invitationId,
            Model model) {
        model.addAttribute("invitation",
                projectService.getInvitationForReceiver(authentication.getName(), invitationId));
        return "invitation_detail";
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public String acceptInvitation(
            Authentication authentication,
            @PathVariable Long invitationId,
            RedirectAttributes redirectAttributes) {
        try {
            Project project = projectService.acceptInvitation(authentication.getName(), invitationId);
            redirectAttributes.addFlashAttribute("successMessage", "프로젝트에 참여했습니다.");
            return "redirect:/project/" + project.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/invitations";
        }
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public String rejectInvitation(
            Authentication authentication,
            @PathVariable Long invitationId,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.rejectInvitation(authentication.getName(), invitationId);
            redirectAttributes.addFlashAttribute("successMessage", "초대를 거절했습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/invitations";
    }

    @PostMapping("/project/{projectId}/delete")
    public String deleteProject(
            Authentication authentication,
            @PathVariable Long projectId) {
        projectService.deleteProject(authentication.getName(), projectId);
        return "redirect:/";
    }

    @PostMapping("/project/{id}/deadline")
    public String updateDeadline(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("deadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {

        System.out.println("deadline update called: " + id + ", " + deadline);

        String email = authentication.getName();
        projectService.updateDeadline(email, id, deadline);

        return "redirect:/project/" + id;
    }

}
