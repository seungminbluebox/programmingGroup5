package kr.ac.dankook.group5.azit.project;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/projects")
    public String createProject(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String description
    ) {
        Project project = projectService.createProject(authentication.getName(), title, description);
        return "redirect:/project/" + project.getId();
    }

    @GetMapping("/project/{projectId}")
    public String detail(
            Authentication authentication,
            @PathVariable Long projectId,
            Model model
    ) {
        Project project = projectService.getProjectForMember(authentication.getName(), projectId);
        model.addAttribute("project", project);
        return "project_detail";
    }

    @PostMapping("/project/{projectId}/links")
    public String addLink(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam String label,
            @RequestParam String url
    ) {
        projectService.addProjectLink(authentication.getName(), projectId, label, url);
        return "redirect:/project/" + projectId;
    }
}
