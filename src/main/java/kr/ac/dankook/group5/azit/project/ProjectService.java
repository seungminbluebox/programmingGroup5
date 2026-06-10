package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;
import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import kr.ac.dankook.group5.azit.user.TechStack;
import kr.ac.dankook.group5.azit.user.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
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
    private final ProjectJoinRequestRepository projectJoinRequestRepository;
    private final ProjectStackRepository projectStackRepository;
    private final ProjectAvailabilityRepository projectAvailabilityRepository;
    private final TechStackRepository techStackRepository;

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

    public List<ProjectMember> getProjectMembers(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        return projectMemberRepository.findAllByProject(project).stream()
                .sorted(Comparator
                        .comparing((ProjectMember projectMember) -> projectMember.getRole() != ProjectMemberRole.OWNER)
                        .thenComparing(projectMember -> projectMember.getJoinedAt(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public ProjectLink addProjectLink(String email, Long projectId, String label, String url) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);
        assertProjectEditable(project);
        return projectLinkRepository.save(new ProjectLink(project, required(label, "링크 이름"), required(url, "링크 주소")));
    }

    @Transactional
    public void updateProjectStatus(String email, Long projectId, ProjectStatus status) {
        Member owner = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectOwner(project, owner);
        project.setStatus(status);
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

    // 프로젝트 소유자인지 확인하기
    private void assertProjectOwner(Project project, Member member) {
        ProjectMember projectMember = projectMemberRepository.findByProjectAndMember(project, member)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 소유자만 접근할 수 있습니다."));

        if (projectMember.getRole() != ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("프로젝트 소유자만 접근할 수 있습니다.");
        }
    }

    private void assertProjectEditable(Project project) {
        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
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

        return projectTaskRepository.findAllByProjectOrderByIdDesc(project).stream()
                .sorted(Comparator.comparing(ProjectTask::isCompleted))
                .toList();
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
        assertProjectEditable(project);

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
        assertProjectEditable(project);

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
    public synchronized void sendInvitation(String email, Long projectId, Long receiverId) {
        Member sender = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectOwner(project, sender);
        assertProjectEditable(project);

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("초대할 회원을 찾을 수 없습니다."));

        createInvitation(project, sender, receiver);
    }

    @Transactional
    public synchronized void sendInvitationByEmail(String senderEmail, Long projectId, String receiverEmail) {
        Member sender = getMemberByEmail(senderEmail);
        Project project = getProjectById(projectId);
        assertProjectOwner(project, sender);
        assertProjectEditable(project);

        Member receiver = memberRepository.findByEmail(required(receiverEmail, "초대할 회원 이메일"))
                .orElseThrow(() -> new IllegalArgumentException("가입된 회원을 찾을 수 없습니다."));

        createInvitation(project, sender, receiver);
    }

    private void createInvitation(Project project, Member sender, Member receiver) {
        if (sender.getId() != null && sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게는 초대할 수 없습니다.");
        }

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
        assertProjectEditable(project);

        if (!projectMemberRepository.existsByProjectAndMember(project, receiver)) {
            addProjectMember(project, receiver);
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

        projectInvitationRepository.deleteAllByProject(project);
        projectJoinRequestRepository.deleteAllByProject(project);
        projectTaskRepository.deleteAllByProject(project);
        projectLinkRepository.deleteAllByProject(project);
        projectMemberRepository.deleteAllByProject(project);
        projectRepository.delete(project);
    }

    public boolean isProjectOwner(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);

        return projectMemberRepository.findByProjectAndMember(project, member)
                .map(projectMember -> projectMember.getRole() == ProjectMemberRole.OWNER)
                .orElse(false);
    }

    private ProjectMember addProjectMember(Project project, Member member) {
        return projectMemberRepository.save(new ProjectMember(project, member, ProjectMemberRole.MEMBER));
    }

    public int getTeamTaskCompletionRate(String email, Long projectId) {
        List<ProjectTask> tasks = getTasks(email, projectId);

        if (tasks.isEmpty()) {
            return 0;
        }

        long completedTaskCount = tasks.stream()
                .filter(ProjectTask::isCompleted)
                .count();

        return (int) Math.round((completedTaskCount * 100.0) / tasks.size());
    }

    public int getDeadlineProgressRate(String email, Long projectId) {
        Project project = getProjectForMember(email, projectId);

        if (project.getDeadline() == null || project.getCreatedAt() == null) {
            return 0;
        }

        LocalDate startDate = project.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate deadline = project.getDeadline();

        long totalDays = ChronoUnit.DAYS.between(startDate, deadline);
        long passedDays = ChronoUnit.DAYS.between(startDate, today);

        if (totalDays <= 0) {
            return 0;
        }

        return (int) Math.min(100, Math.max(0, passedDays * 100 / totalDays));
    }

    @Transactional
    public void updateDeadline(String email, Long projectId, LocalDate deadline) {
        Project project = getProjectForMember(email, projectId);

        if (!isProjectOwner(email, projectId)) {
            throw new IllegalArgumentException("프로젝트 소유자만 마감일을 수정할 수 있습니다.");
        }

        System.out.println("요청 deadline = " + deadline);
        System.out.println("저장 전 deadline = " + project.getDeadline());

        project.setDeadline(deadline);
        projectRepository.save(project);

        Project savedProject = projectRepository.findById(projectId)
                .orElseThrow();

        System.out.println("저장 후 deadline = " + savedProject.getDeadline());
    }

    public String getDeadlineDday(String email, Long projectId) {
        Project project = getProjectForMember(email, projectId);

        if (project.getDeadline() == null) {
            return "미설정";
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), project.getDeadline());

        if (days > 0) {
            return "D-" + days;
        }

        if (days == 0) {
            return "D-Day";
        }

        return "D+" + Math.abs(days);
    }

    @Transactional
    public void addProjectRequiredStacks(Long projectId, List<Long> stackIds) {
        Project project = getProjectById(projectId);
        projectStackRepository.deleteAllByProject(project);

        if (stackIds != null && !stackIds.isEmpty()) {
            for (Long stackId : stackIds) {
                TechStack techStack = techStackRepository.findById(stackId)
                        .orElseThrow(() -> new IllegalArgumentException("기술 스택을 찾을 수 없습니다."));
                projectStackRepository.save(new ProjectStack(project, techStack, true));
            }
        }
    }

    @Transactional
    public void addProjectAvailabilities(Long projectId, List<String> days, List<String> starts, List<String> ends) {
        Project project = getProjectById(projectId);
        projectAvailabilityRepository.deleteAllByProject(project);

        if (days != null && !days.isEmpty()) {
            for (int i = 0; i < days.size(); i++) {
                try {
                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(days.get(i));
                    LocalTime startTime = LocalTime.parse(starts.get(i));
                    LocalTime endTime = LocalTime.parse(ends.get(i));

                    projectAvailabilityRepository.save(
                            new ProjectAvailability(project, dayOfWeek, startTime, endTime));
                } catch (Exception e) {
                    System.out.println("Failed to parse project availability: " + e.getMessage());
                }
            }
        }
    }

    public int getDeadlineRemainingRate(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        if (project.getDeadline() == null || project.getCreatedAt() == null) {
            return 0;
        }

        LocalDate startDate = project.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate deadline = project.getDeadline();

        long totalDays = ChronoUnit.DAYS.between(startDate, deadline);
        long remainingDays = ChronoUnit.DAYS.between(today, deadline);

        if (totalDays <= 0) {
            return today.isAfter(deadline) ? 0 : 100;
        }

        int rate = (int) Math.round((remainingDays * 100.0) / totalDays);

        return Math.min(100, Math.max(0, rate));
    }

    public long getDeadlineRemainingDays(String email, Long projectId) {
        Member member = getMemberByEmail(email);
        Project project = getProjectById(projectId);
        assertProjectMember(project, member);

        if (project.getDeadline() == null) {
            return 0;
        }

        return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), project.getDeadline()));
    }
}
