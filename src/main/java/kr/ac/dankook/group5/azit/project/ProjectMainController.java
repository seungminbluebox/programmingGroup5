package kr.ac.dankook.group5.azit.project;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProjectMainController {

    private final ProjectRecommendationService projectRecommendationService;
    private final ProjectJoinRequestService projectJoinRequestService;
    private final ProjectService projectService;

    @GetMapping("/main")
    public String main(Authentication authentication, Model model) {
        boolean loggedIn = isLoggedIn(authentication);
        String email = loggedIn ? authentication.getName() : null;

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("mainProjects", projectRecommendationService.findMainProjects(email));
        model.addAttribute("pendingInvitations", loggedIn ? projectService.getPendingInvitations(email) : List.of());

        return "main";
    }

    @PostMapping("/main/projects/{projectId}/join-requests")
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

        return "redirect:/main";
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
