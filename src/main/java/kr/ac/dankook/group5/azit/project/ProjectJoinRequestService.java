package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectJoinRequestService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectJoinRequestRepository projectJoinRequestRepository;

    @Transactional
    public synchronized void apply(String email, Long projectId) {
        Member applicant = getMemberByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("진행 중 프로젝트에만 가입신청할 수 있습니다.");
        }

        if (projectMemberRepository.existsByProjectAndMember(project, applicant)) {
            throw new IllegalArgumentException("이미 참여 중인 프로젝트입니다.");
        }

        if (projectJoinRequestRepository.existsByProjectAndApplicantAndStatus(
                project,
                applicant,
                ProjectJoinRequestStatus.PENDING)) {
            throw new IllegalArgumentException("이미 가입신청한 프로젝트입니다.");
        }

        projectJoinRequestRepository.save(new ProjectJoinRequest(project, applicant));
    }

    public List<ProjectJoinRequest> getPendingRequestsForOwner(String ownerEmail) {
        Member owner = getMemberByEmail(ownerEmail);

        Set<String> seenRequests = new HashSet<>();
        return projectJoinRequestRepository.findAllByStatus(ProjectJoinRequestStatus.PENDING).stream()
                .filter(request -> isProjectOwner(request.getProject(), owner))
                .filter(request -> seenRequests.add(request.getProject().getId() + ":" + request.getApplicant().getId()))
                .toList();
    }

    @Transactional
    public void accept(String ownerEmail, Long requestId) {
        Member owner = getMemberByEmail(ownerEmail);
        ProjectJoinRequest request = getPendingRequest(requestId);
        Project project = request.getProject();
        Member applicant = request.getApplicant();

        assertOwner(project, owner);
        assertProjectEditable(project);

        if (!projectMemberRepository.existsByProjectAndMember(project, applicant)) {
            projectMemberRepository.save(new ProjectMember(project, applicant, ProjectMemberRole.MEMBER));
        }

        projectJoinRequestRepository.findAllByProjectAndApplicantAndStatus(
                project,
                applicant,
                ProjectJoinRequestStatus.PENDING)
                .forEach(ProjectJoinRequest::accept);
    }

    @Transactional
    public void reject(String ownerEmail, Long requestId) {
        Member owner = getMemberByEmail(ownerEmail);
        ProjectJoinRequest request = getPendingRequest(requestId);

        Project project = request.getProject();
        Member applicant = request.getApplicant();

        assertOwner(project, owner);
        projectJoinRequestRepository.findAllByProjectAndApplicantAndStatus(
                project,
                applicant,
                ProjectJoinRequestStatus.PENDING)
                .forEach(ProjectJoinRequest::reject);
    }

    private ProjectJoinRequest getPendingRequest(Long requestId) {
        ProjectJoinRequest request = projectJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("가입신청을 찾을 수 없습니다."));

        if (request.getStatus() != ProjectJoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 가입신청입니다.");
        }

        return request;
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    private void assertOwner(Project project, Member member) {
        if (!isProjectOwner(project, member)) {
            throw new IllegalArgumentException("프로젝트 소유자만 가입신청을 처리할 수 있습니다.");
        }
    }

    private boolean isProjectOwner(Project project, Member member) {
        return projectMemberRepository.findByProjectAndMember(project, member)
                .map(projectMember -> projectMember.getRole() == ProjectMemberRole.OWNER)
                .orElse(false);
    }

    private void assertProjectEditable(Project project) {
        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("완료 또는 중단된 프로젝트의 가입신청은 수락할 수 없습니다.");
        }
    }
}
