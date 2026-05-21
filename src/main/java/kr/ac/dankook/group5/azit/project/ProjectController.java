package kr.ac.dankook.group5.azit.project;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/projects")
    public String createProject(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String description) {
        Project project = projectService.createProject(authentication.getName(), title, description);
        return "redirect:/project/" + project.getId();
    }

    @GetMapping("/project/{projectId}")
    public String detail(
            Authentication authentication,
            @PathVariable Long projectId,
            Model model) {
        String email = authentication.getName();

        Project project = projectService.getProjectForMember(email, projectId);

        model.addAttribute("project", project);
        model.addAttribute("tasks", projectService.getTasks(email, projectId));
        model.addAttribute("myTaskCompletionRate", projectService.getMyTaskCompletionRate(email, projectId));
        model.addAttribute("myCompletedTaskCount", projectService.getMyCompletedTaskCount(email, projectId));
        model.addAttribute("inviteCandidates", projectService.getInviteCandidates(email, projectId));
        model.addAttribute("pendingInvitations", projectService.getPendingInvitations(email));
        model.addAttribute("projectOwner", projectService.isProjectOwner(email, projectId));

        return "project_detail";
    }

    @PostMapping("/project/{projectId}/links")
    public String addLink(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String label,
            @RequestParam String url) {
        projectService.addProjectLink(authentication.getName(), projectId, label, url);
        return "redirect:/project/" + projectId;
    }

    @PostMapping("/project/{projectId}/members")
    public String addMember(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String memberEmail,
            RedirectAttributes redirectAttributes) {
        try {
            projectService.addMemberToProject(authentication.getName(), projectId, memberEmail);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/project/" + projectId;
    }

    @PostMapping("/project/{projectId}/tasks")
    public String addTask(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String title,
            @RequestParam Long assigneeId) {
        projectService.addTask(authentication.getName(), projectId, title, assigneeId);
        return "redirect:/project/" + projectId;
    }

    @PostMapping("/project/{projectId}/tasks/{taskId}/toggle")
    public String toggleTask(
            Authentication authentication,
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        projectService.toggleTask(authentication.getName(), projectId, taskId);
        return "redirect:/project/" + projectId;
    }

    @PostMapping("/project/{projectId}/invitations")
    public String sendInvitation(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam Long receiverId) {
        projectService.sendInvitation(authentication.getName(), projectId, receiverId);
        return "redirect:/project/" + projectId;
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
            @PathVariable Long invitationId) {
        Project project = projectService.acceptInvitation(authentication.getName(), invitationId);
        return "redirect:/project/" + project.getId();
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public String rejectInvitation(
            Authentication authentication,
            @PathVariable Long invitationId) {
        projectService.rejectInvitation(authentication.getName(), invitationId);
        return "redirect:/invitations";
    }

    @PostMapping("/project/{projectId}/delete")
    public String deleteProject(
            Authentication authentication,
            @PathVariable Long projectId) {
        projectService.deleteProject(authentication.getName(), projectId);
        return "redirect:/";
    }

}
