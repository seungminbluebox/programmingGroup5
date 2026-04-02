package kr.ac.dankook.group5.azit.auth;

import kr.ac.dankook.group5.azit.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
            @RequestParam String password,
            @RequestParam String nickname) {
        userService.register(email, password, nickname);
        return "redirect:/login";
    }
}
