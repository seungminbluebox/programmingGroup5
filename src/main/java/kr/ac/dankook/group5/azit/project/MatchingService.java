package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.MemberStack;
import kr.ac.dankook.group5.azit.user.MemberStackRepository;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final ProjectRepository projectRepository;
    private final ProjectStackRepository projectStackRepository;
    private final ProjectAvailabilityRepository projectAvailabilityRepository;
    private final MemberRepository memberRepository;
    private final MemberStackRepository memberStackRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;

    public List<RecommendedMember> recommendMembers(Long projectId) {
        Project project = getProjectById(projectId);
        List<ProjectStack> requiredStacks = projectStackRepository.findAllByProjectAndRequired(project, true);
        List<ProjectAvailability> projectAvailabilities = projectAvailabilityRepository.findAllByProject(project);

        Set<Long> requiredStackIds = requiredStacks.stream()
                .map(stack -> stack.getTechStack().getId())
                .collect(Collectors.toSet());

        boolean availabilityRequired = !projectAvailabilities.isEmpty();

        return memberRepository.findAll().stream()
                .filter(member -> Boolean.TRUE.equals(member.getIsSearching()))
                .filter(member -> !projectMemberRepository.existsByProjectAndMember(project, member))
                .filter(member -> !projectInvitationRepository.existsByProjectAndReceiverAndStatus(
                        project,
                        member,
                        ProjectInvitationStatus.PENDING))
                .map(member -> buildRecommendation(member, requiredStackIds, projectAvailabilities, availabilityRequired))
                .filter(rec -> rec != null)
                .sorted(Comparator.comparingInt(RecommendedMember::score).reversed())
                .toList();
    }

    private RecommendedMember buildRecommendation(
            Member member,
            Set<Long> requiredStackIds,
            List<ProjectAvailability> projectAvailabilities,
            boolean availabilityRequired) {

        List<MemberStack> memberStacks = memberStackRepository.findAllByMember(member);
        Set<Long> memberStackIds = memberStacks.stream()
                .map(stack -> stack.getTechStack().getId())
                .collect(Collectors.toSet());

        if (!requiredStackIds.isEmpty() && !memberStackIds.containsAll(requiredStackIds)) {
            return null;
        }

        int availabilityMatchCount = calculateAvailabilityMatchCount(projectAvailabilities, member.getAvailabilities());
        if (availabilityRequired && availabilityMatchCount == 0) {
            return null;
        }

        // 필수 스택 개수별 기본 점수 (100 / N)
        int baseStackScore = requiredStackIds.isEmpty() ? 0 : 100 / requiredStackIds.size();
        int totalRequiredStackScore = 0;
        
        // 각 필수 스택별로 경력 기반 점수 계산
        for (Long requiredStackId : requiredStackIds) {
            totalRequiredStackScore += baseStackScore;
            
            // 이 필수 스택의 경력 가산점 찾기
            for (MemberStack memberStack : memberStacks) {
                if (memberStack.getTechStack().getId().equals(requiredStackId)) {
                    Integer expYears = memberStack.getExpYears();
                    if (expYears != null && expYears > 0) {
                        // 0.5년(6개월)당 0.2 X 필수스택점수
                        int bonusScore = (int) (expYears * 0.4 * baseStackScore);
                        totalRequiredStackScore += bonusScore;
                    }
                    break;
                }
            }
        }
        
        int score = totalRequiredStackScore;
        
        // 추가 스택 (우대사항): 있으면 고정 1점만 추가 (개수와 무관 >)
        int additionalStackCount = memberStackIds.size() - requiredStackIds.size();
        if (additionalStackCount > 0) {
            score += 1;
        }
        
        // 가용시간: 매우 낮은 가중치 (최대 3점)
        score += Math.min(availabilityMatchCount, 3);

        return new RecommendedMember(member, score, memberStackIds.size(), availabilityMatchCount);
    }

    private int calculateAvailabilityMatchCount(
            List<ProjectAvailability> projectAvailabilities,
            List<Routine> memberAvailabilities) {
        if (projectAvailabilities.isEmpty() || memberAvailabilities.isEmpty()) {
            return 0;
        }

        return (int) projectAvailabilities.stream()
                .filter(projectAvailability -> memberAvailabilities.stream()
                        .anyMatch(memberAvailability -> isOverlap(projectAvailability, memberAvailability)))
                .count();
    }

    private boolean isOverlap(ProjectAvailability projectAvailability, Routine memberAvailability) {
        if (projectAvailability.getDayOfWeek() != memberAvailability.getDayOfWeek()) {
            return false;
        }

        LocalTime projectStart = projectAvailability.getStartTime();
        LocalTime projectEnd = projectAvailability.getEndTime();
        LocalTime memberStart = memberAvailability.getStartTime();
        LocalTime memberEnd = memberAvailability.getEndTime();

        return projectStart != null && projectEnd != null && memberStart != null && memberEnd != null
                && !projectEnd.isBefore(memberStart)
                && !memberEnd.isBefore(projectStart);
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    }

    public static class RecommendedMember {
        private final Member member;
        private final int score;
        private final int stackCount;
        private final int availabilityCount;

        public RecommendedMember(Member member, int score, int stackCount, int availabilityCount) {
            this.member = member;
            this.score = score;
            this.stackCount = stackCount;
            this.availabilityCount = availabilityCount;
        }

        public Member member() {
            return member;
        }

        public int score() {
            return score;
        }

        public int stackCount() {
            return stackCount;
        }

        public int availabilityCount() {
            return availabilityCount;
        }
    }
}
