package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import kr.ac.dankook.group5.azit.user.MemberStack;
import kr.ac.dankook.group5.azit.user.MemberStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRecommendationService {

    private final MemberRepository memberRepository;
    private final MemberStackRepository memberStackRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectJoinRequestRepository projectJoinRequestRepository;

    public List<ProjectRecommendation> findMainProjects(String email) {
        List<Project> projects = projectRepository.findAllByStatus(ProjectStatus.IN_PROGRESS);
        Member member = getMember(email);
        List<String> stackNames = getStackNames(member);

        return projects.stream()
                .filter(project -> member == null || !projectMemberRepository.existsByProjectAndMember(project, member))
                .map(project -> toRecommendation(project, stackNames, member))
                .sorted(Comparator.comparingLong(ProjectRecommendation::matchCount).reversed()
                        .thenComparing(recommendation -> recommendation.project().getId()))
                .toList();
    }

    private Member getMember(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return memberRepository.findByEmail(email).orElse(null);
    }

    private List<String> getStackNames(Member member) {
        if (member == null) {
            return List.of();
        }

        return memberStackRepository.findAllByMember(member).stream()
                .map(MemberStack::getTechStack)
                .filter(techStack -> techStack != null && techStack.getStackName() != null)
                .map(techStack -> techStack.getStackName().trim())
                .filter(stackName -> !stackName.isEmpty())
                .distinct()
                .toList();
    }

    private ProjectRecommendation toRecommendation(Project project, List<String> stackNames, Member member) {
        String searchableText = ((project.getTitle() == null ? "" : project.getTitle()) + " "
                + (project.getDescription() == null ? "" : project.getDescription()))
                .toLowerCase(Locale.ROOT);

        List<String> matchedStacks = stackNames.stream()
                .filter(stackName -> searchableText.contains(stackName.toLowerCase(Locale.ROOT)))
                .toList();
        List<ProjectMember> projectMembers = projectMemberRepository.findAllByProject(project);
        List<String> memberNames = projectMembers.stream()
                .map(ProjectMember::getMember)
                .filter(projectMember -> projectMember != null && projectMember.getName() != null)
                .map(memberItem -> memberItem.getName().trim())
                .filter(memberName -> !memberName.isEmpty())
                .toList();
        Member owner = projectMembers.stream()
                .filter(projectMember -> projectMember.getRole() == ProjectMemberRole.OWNER)
                .map(ProjectMember::getMember)
                .filter(projectOwner -> projectOwner != null)
                .findFirst()
                .orElse(null);
        Long ownerId = owner == null ? null : owner.getId();
        String ownerName = resolveOwnerName(owner);
        boolean alreadyMember = member != null && projectMembers.stream()
                .anyMatch(projectMember -> projectMember.getMember() != null
                        && projectMember.getMember().getId() != null
                        && projectMember.getMember().getId().equals(member.getId()));
        boolean alreadyApplied = member != null && projectJoinRequestRepository.existsByProjectAndApplicantAndStatus(
                project,
                member,
                ProjectJoinRequestStatus.PENDING);

        return new ProjectRecommendation(
                project,
                matchedStacks.size(),
                matchedStacks,
                projectMembers.size(),
                memberNames,
                ownerId,
                ownerName,
                alreadyMember,
                alreadyApplied);
    }

    private String resolveOwnerName(Member owner) {
        if (owner == null) {
            return "미정";
        }
        if (owner.getName() != null && !owner.getName().trim().isEmpty()) {
            return owner.getName().trim();
        }
        if (owner.getEmail() != null && !owner.getEmail().trim().isEmpty()) {
            return owner.getEmail().trim();
        }
        return "미정";
    }
}
