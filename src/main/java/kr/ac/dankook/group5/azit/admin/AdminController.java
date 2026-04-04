package kr.ac.dankook.group5.azit.admin;

import kr.ac.dankook.group5.azit.user.TechStack;
import kr.ac.dankook.group5.azit.user.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TechStackRepository techStackRepository;

    @GetMapping("/stacks")
    public String manageStacks(Model model) {
        model.addAttribute("stacks", techStackRepository.findAll());
        return "admin_stacks";
    }

    @PostMapping("/stacks/add")
    public String addStack(@RequestParam String stackName, @RequestParam(required = false) String iconUrl) {
        if (stackName != null && !stackName.trim().isEmpty()) {
            TechStack stack = new TechStack();
            stack.setStackName(stackName.trim());
            stack.setIconUrl(iconUrl != null && !iconUrl.trim().isEmpty() ? iconUrl.trim() : null);
            techStackRepository.save(stack);
        }
        return "redirect:/admin/stacks";
    }

    @PostMapping("/stacks/delete/{id}")
    public String deleteStack(@PathVariable Long id) {
        techStackRepository.deleteById(id);
        return "redirect:/admin/stacks";
    }
}