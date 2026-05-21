package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectLinkRepository projectLinkRepository;
    private final MemberRepository memberRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectInvitationRepository projectInvitationRepository;

    @Transactional
    public Project createProject(String ownerEmail, String title, String description) {
        Member owner = getMemberByEmail(ownerEmail);
        Project project = projectRepository
                .save(new Project(required(title, "프로젝트 이름"), required(description, "프로젝트 설명")));
        projectMemberRepository.save(new ProjectMember(project, owner, ProjectMemberRole.OWNER));
        return project;
    }

    public List<Project> findProjectsForMember(String email) {
        Member member = getMemberByEmail(email);
        return projectMemberRepository.findAllByMember(member).stream()
                .map(ProjectMember::getProject)
                .toList();
    }

    public Project getProjectForMember(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);
        return project;
    }

    @Transactional
    public ProjectLink addProjectLink(String email, Long projectId, String label, String url) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);
        return projectLinkRepository.save(new ProjectLink(project, required(label, "링크 이름"), required(url, "링크 주소")));
    }

    //프로젝트에 멤버 추가 (프로젝트 소유자만)
    @Transactional
    public ProjectMember addMemberToProject(String ownerEmail, Long projectId, String memberEmail) {
        Member owner = getMemberByEmail(ownerEmail);
        Project project = getProjectById(projectId);
        assertProjectOwner(project, owner);

        Member member = getMemberByEmail(memberEmail);
        if (projectMemberRepository.existsByProjectAndMember(project, member)) {
            throw new IllegalArgumentException("이미 프로젝트 멤버입니다.");
        }

        return projectMemberRepository.save(new ProjectMember(project, member, ProjectMemberRole.MEMBER));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    }

    private void assertProjectMember(Project project, Member member) {
        if (!projectMemberRepository.existsByProjectAndMember(project, member)) {
            throw new IllegalArgumentException("프로젝트 멤버만 접근할 수 있습니다.");
        }
    }

    //프로젝트 소유자인지 확인하기
    private void assertProjectOwner(Project project, Member member) {
        ProjectMember projectMember = projectMemberRepository.findByProjectAndMember(project, member)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 소유자만 접근할 수 있습니다."));

        if (projectMember.getRole() != ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("프로젝트 소유자만 접근할 수 있습니다.");
        }
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "을 입력해 주세요.");
        }
        return value.trim();
    }

    public List<ProjectTask> getTasks(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        return projectTaskRepository.findAllByProjectOrderByIdDesc(project);
    }

    public int getMyTaskCompletionRate(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        List<ProjectTask> myTasks = projectTaskRepository.findAllByProjectAndAssignee(project, member);

        if (myTasks.isEmpty()) {
            return 0;
        }

        long completedCount = myTasks.stream()
                .filter(ProjectTask::isCompleted)
                .count();

        return (int) Math.round((completedCount * 100.0) / myTasks.size());
    }

    public long getMyCompletedTaskCount(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        return projectTaskRepository.findAllByProjectAndAssignee(project, member).stream()
                .filter(ProjectTask::isCompleted)
                .count();
    }

    @Transactional
    public ProjectTask addTask(String email, Long projectId, String title, Long assigneeId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        Member assignee = memberRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다."));

        assertProjectMember(project, assignee);

        return projectTaskRepository.save(new ProjectTask(project, assignee, required(title, "태스크 제목")));
    }

    @Transactional
    public void toggleTask(String email, Long projectId, Long taskId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("태스크를 찾을 수 없습니다."));

        if (!task.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("해당 프로젝트의 태스크가 아닙니다.");
        }

        task.toggleCompleted();
    }

    public List<Member> getInviteCandidates(String email, Long projectId) {
        Member currentMember = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, currentMember);

        return memberRepository.findAll().stream()
                .filter(member -> !member.getId().equals(currentMember.getId()))
                .filter(member -> !projectMemberRepository.existsByProjectAndMember(project, member))
                .filter(member -> !projectInvitationRepository.existsByProjectAndReceiverAndStatus(
                        project,
                        member,
                        ProjectInvitationStatus.PENDING))
                .toList();
    }

    @Transactional
    public void sendInvitation(String email, Long projectId, Long receiverId) {
        Member sender = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, sender);

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("초대할 회원을 찾을 수 없습니다."));

        if (projectMemberRepository.existsByProjectAndMember(project, receiver)) {
            throw new IllegalArgumentException("이미 프로젝트에 참여 중인 회원입니다.");
        }

        if (projectInvitationRepository.existsByProjectAndReceiverAndStatus(project, receiver,
                ProjectInvitationStatus.PENDING)) {
            throw new IllegalArgumentException("이미 초대를 보낸 회원입니다.");
        }

        projectInvitationRepository.save(new ProjectInvitation(project, sender, receiver));
    }

    public List<ProjectInvitation> getPendingInvitations(String email) {
        Member receiver = getMemberByEmail(email);
        return projectInvitationRepository.findAllByReceiverAndStatus(receiver, ProjectInvitationStatus.PENDING);
    }

    public ProjectInvitation getInvitationForReceiver(String email, Long invitationId) {
        Member receiver = getMemberByEmail(email);

        ProjectInvitation invitation = projectInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다."));

        if (!invitation.getReceiver().getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("본인에게 온 초대만 확인할 수 있습니다.");
        }

        return invitation;
    }

    @Transactional
    public Project acceptInvitation(String email, Long invitationId) {
        ProjectInvitation invitation = getInvitationForReceiver(email, invitationId);

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 초대입니다.");
        }

        Project project = invitation.getProject();
        Member receiver = invitation.getReceiver();

        if (!projectMemberRepository.existsByProjectAndMember(project, receiver)) {
            projectMemberRepository.save(new ProjectMember(project, receiver, ProjectMemberRole.MEMBER));
        }

        invitation.accept();

        return project;
    }

    @Transactional
    public void rejectInvitation(String email, Long invitationId) {
        ProjectInvitation invitation = getInvitationForReceiver(email, invitationId);

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 초대입니다.");
        }

        invitation.reject();
    }

    @Transactional
    public void deleteProject(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);

        ProjectMember projectMember = projectMemberRepository.findByProjectAndMember(project, member)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 멤버만 접근할 수 있습니다."));

        if (projectMember.getRole() != ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("프로젝트 소유자만 삭제할 수 있습니다.");
        }

        projectRepository.delete(project);
    }
}
