package kr.ac.dankook.group5.azit.project;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class SidebarModelAdvice {

    private final MemberRepository memberRepository;
    private final ProjectService projectService;

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("sidebarMemberName")
    public String sidebarMemberName(Authentication authentication) {
        if (!isSignedIn(authentication)) {
            return null;
        }

        return memberRepository.findByEmail(authentication.getName())
                .map(member -> member.getName())
                .orElse(authentication.getName());
    }

    @ModelAttribute("sidebarMemberProfileUrl")
    public String sidebarMemberProfileUrl(Authentication authentication) {
        if (!isSignedIn(authentication)) {
            return null;
        }

        return memberRepository.findByEmail(authentication.getName())
                .map(member -> member.getProfileUrl())
                .orElse(null);
    }

    @ModelAttribute("sidebarProjects")
    public List<Project> sidebarProjects(Authentication authentication) {
        if (!isSignedIn(authentication)) {
            return List.of();
        }

        return projectService.findProjectsForMember(authentication.getName());
    }

    private boolean isSignedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
