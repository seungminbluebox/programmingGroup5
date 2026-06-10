package kr.ac.dankook.group5.azit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/techstacks")
@RequiredArgsConstructor
public class TechStackController {

    private final TechStackRepository techStackRepository;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<TechStack>> getAllTechStacks() {
        return ResponseEntity.ok(techStackRepository.findAll());
    }

    @GetMapping("/debug/members")
    public ResponseEntity<?> debugMembers() {
        return ResponseEntity.ok(memberRepository.findAll());
    }

    @PostMapping("/add")
    public ResponseEntity<TechStack> addTechStack(
            @RequestParam String stackName,
            @RequestParam(required = false) String iconUrl) {
        if (stackName == null || stackName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        TechStack stack = new TechStack();
        stack.setStackName(stackName.trim());
        stack.setIconUrl(iconUrl != null && !iconUrl.trim().isEmpty() ? iconUrl.trim() : null);
        return ResponseEntity.ok(techStackRepository.save(stack));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTechStack(@PathVariable Long id) {
        if (!techStackRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        techStackRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}