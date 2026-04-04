package kr.ac.dankook.group5.azit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;
    private final MemberStackRepository memberStackRepository;

    @Transactional(readOnly = true)
    public List<TechStack> getAllTechStacks() {
        return techStackRepository.findAll();
    }

    @Transactional
    public void updateTechStacks(String email, List<Long> stackIds) {
        Member member = getMemberByEmail(email);

        // 기존 스택 삭제
        memberStackRepository.deleteByMember(member);

        // 새로운 스택 추가
        if (stackIds != null) {
            for (Long stackId : stackIds) {
                TechStack techStack = techStackRepository.findById(stackId)
                        .orElseThrow(() -> new IllegalArgumentException("기술 스택을 찾을 수 없습니다. ID: " + stackId));

                MemberStack memberStack = new MemberStack();
                memberStack.setMember(member);
                memberStack.setTechStack(techStack);
                memberStackRepository.save(memberStack);
            }
        }
    }

    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public void updateBasicProfile(String email, String name, Integer age, String bio, MultipartFile profileImage) {
        Member member = getMemberByEmail(email);
        member.setName(name);
        member.setAge(age);
        member.setBio(bio);

        // 프로필 사진 업로드 처리 로직
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 프로젝트 최상단에 uploads 폴더 생성
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 파일명 중복을 피하기 위해 UUID 사용
                String originalFilename = profileImage.getOriginalFilename();
                String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

                Path filePath = uploadPath.resolve(storedFileName);
                Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // DB에는 웹 접근 경로를 저장
                member.setProfileUrl("/uploads/" + storedFileName);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("프로필 사진 업로드 중 오류가 발생했습니다.");
            }
        }
    }

    @Transactional
    public void updateCareersAndAvailabilities(String email,
            List<String> careerTitles, List<String> careerDescriptions, List<String> careerLinks,
            List<String> availDays, List<String> availStarts, List<String> availEnds) {

        Member member = getMemberByEmail(email);

        // Update Careers
        member.getCareers().clear();
        if (careerTitles != null) {
            for (int i = 0; i < careerTitles.size(); i++) {
                if (careerTitles.get(i) == null || careerTitles.get(i).trim().isEmpty())
                    continue;
                Career career = new Career();
                career.setMember(member);
                career.setTitle(careerTitles.get(i).trim());
                career.setDescription(
                        careerDescriptions != null && i < careerDescriptions.size() ? careerDescriptions.get(i).trim()
                                : null);
                career.setLinkUrl(careerLinks != null && i < careerLinks.size() ? careerLinks.get(i).trim() : null);
                member.getCareers().add(career);
            }
        }

        // Update Availabilities
        member.getAvailabilities().clear();
        if (availDays != null && availStarts != null && availEnds != null) {
            for (int i = 0; i < availDays.size(); i++) {
                if (availStarts.get(i) == null || availStarts.get(i).trim().isEmpty())
                    continue;
                if (availEnds.get(i) == null || availEnds.get(i).trim().isEmpty())
                    continue;

                Availability availability = new Availability();
                availability.setMember(member);
                try {
                    availability.setDayOfWeek(Availability.DayOfWeek.valueOf(availDays.get(i)));
                    availability.setStartTime(LocalTime.parse(availStarts.get(i)));
                    availability.setEndTime(LocalTime.parse(availEnds.get(i)));
                    member.getAvailabilities().add(availability);
                } catch (Exception e) {
                    System.out.println("Failed to parse availability: " + e.getMessage());
                }
            }
        }
    }
}
