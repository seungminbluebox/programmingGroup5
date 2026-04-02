package kr.ac.dankook.group5.azit.auth;

import kr.ac.dankook.group5.azit.user.User;
import kr.ac.dankook.group5.azit.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            String email = authentication.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                model.addAttribute("nickname", user.getNickname());
            });
        }
        return "home";
    }
}
