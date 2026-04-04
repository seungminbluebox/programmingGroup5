package kr.ac.dankook.group5.azit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 프로필 조회 (ID 기반)
    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        System.out.println("====== [DEBUG] /profile/" + id + " 접속 시도 ======");
        Member targetMember = profileService.getMemberById(id);
        System.out.println("[DEBUG] 조회된 회원이름: " + targetMember.getName() + " | 이메일: " + targetMember.getEmail());
        System.out.println("[DEBUG] 회원 보유 기술스택 수: "
                + (targetMember.getMemberStacks() != null ? targetMember.getMemberStacks().size() : 0));

        boolean isOwnProfile = false;
        if (userDetails != null) {
            Member currentUser = profileService.getMemberByEmail(userDetails.getUsername());
            isOwnProfile = targetMember.getId().equals(currentUser.getId());
            System.out.println("[DEBUG] 로그인 사용자: " + currentUser.getEmail() + " | 본인 계정 여부: " + isOwnProfile);
        } else {
            System.out.println("[DEBUG] 비로그인 사용자 열람");
        }

        List<Long> selectedStackIds = targetMember.getMemberStacks().stream()
                .map(ms -> ms.getTechStack().getId())
                .toList();

        model.addAttribute("member", targetMember);
        model.addAttribute("allStacks", profileService.getAllTechStacks());
        model.addAttribute("selectedStackIds", selectedStackIds);
        model.addAttribute("isOwnProfile", isOwnProfile);

        System.out.println("====== [DEBUG] 데이터 Model 담기 성공, profile.html 렌더링 호출 ======");
        return "profile";
    }

    // 기술 스택 업데이트 처리
    @PostMapping("/profile/stacks/update")
    public String updateStacks(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<Long> stackIds) {
        if (userDetails == null)
            return "redirect:/login";

        profileService.updateTechStacks(userDetails.getUsername(), stackIds);
        Member member = profileService.getMemberByEmail(userDetails.getUsername());
        return "redirect:/profile/" + member.getId();
    }

    // 내 프로필 리다이렉트 (ID 기반 URL로 이동시켜줌)
    @GetMapping("/profile/me")
    public String myProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";

        Member member = profileService.getMemberByEmail(userDetails.getUsername());
        return "redirect:/profile/" + member.getId();
    }

    // 기본 프로필 수정 처리
    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String name,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) List<Long> stackIds,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam(value = "careerTitle", required = false) List<String> careerTitles,
            @RequestParam(value = "careerDesc", required = false) List<String> careerDescriptions,
            @RequestParam(value = "careerLink", required = false) List<String> careerLinks,
            @RequestParam(value = "availDay", required = false) List<String> availDays,
            @RequestParam(value = "availStart", required = false) List<String> availStarts,
            @RequestParam(value = "availEnd", required = false) List<String> availEnds) {

        if (userDetails == null)
            return "redirect:/login";

        Member member = profileService.getMemberByEmail(userDetails.getUsername());
        profileService.updateBasicProfile(userDetails.getUsername(), name, age, bio, profileImage);
        profileService.updateTechStacks(userDetails.getUsername(), stackIds);
        profileService.updateCareersAndAvailabilities(
                userDetails.getUsername(),
                careerTitles, careerDescriptions, careerLinks,
                availDays, availStarts, availEnds);
        return "redirect:/profile/" + member.getId();
    }
}
