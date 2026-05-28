package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import kr.ac.dankook.group5.azit.user.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectLinkRepository projectLinkRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProjectTaskRepository projectTaskRepository;

    @Mock
    private ProjectInvitationRepository projectInvitationRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProjectStoresProjectAndAddsCreatorAsOwner() {
        Member creator = new Member();
        creator.setEmail("owner@example.com");
        creator.setName("Owner");
        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(creator));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project project = projectService.createProject("owner@example.com", "Portfolio Matching", "Build matching app");

        assertThat(project.getTitle()).isEqualTo("Portfolio Matching");
        assertThat(project.getDescription()).isEqualTo("Build matching app");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getProject()).isSameAs(project);
        assertThat(memberCaptor.getValue().getMember()).isSameAs(creator);
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(ProjectMemberRole.OWNER);
    }

    @Test
    void findProjectsForMemberReturnsMembershipProjects() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project first = new Project();
        first.setTitle("First");
        Project second = new Project();
        second.setTitle("Second");
        ProjectMember firstMembership = new ProjectMember(first, member, ProjectMemberRole.OWNER);
        ProjectMember secondMembership = new ProjectMember(second, member, ProjectMemberRole.MEMBER);

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectMemberRepository.findAllByMember(member)).thenReturn(List.of(firstMembership, secondMembership));

        List<Project> projects = projectService.findProjectsForMember("member@example.com");

        assertThat(projects).containsExactly(first, second);
    }

    @Test
    void addProjectLinkRequiresProjectMembership() {
        Member stranger = new Member();
        stranger.setEmail("stranger@example.com");
        Project project = new Project();
        project.setTitle("Private");

        when(memberRepository.findByEmail("stranger@example.com")).thenReturn(Optional.of(stranger));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndMember(project, stranger)).thenReturn(false);

        assertThatThrownBy(() -> projectService.addProjectLink("stranger@example.com", 10L, "GitHub", "https://github.com/example/repo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("프로젝트 멤버만");
    }

    @Test
    void getTasksReturnsIncompleteTasksBeforeCompletedTasks() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = new Project();
        ProjectTask completedTask = new ProjectTask(project, member, "Completed");
        completedTask.toggleCompleted();
        ProjectTask incompleteTask = new ProjectTask(project, member, "Incomplete");

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndMember(project, member)).thenReturn(true);
        when(projectTaskRepository.findAllByProjectOrderByIdDesc(project)).thenReturn(List.of(completedTask, incompleteTask));

        List<ProjectTask> tasks = projectService.getTasks("member@example.com", 1L);

        assertThat(tasks).containsExactly(incompleteTask, completedTask);
    }

    @Test
    void sendInvitationByEmailRequiresOwnerRole() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = new Project();
        project.setTitle("Team Project");

        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.MEMBER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));

        assertThatThrownBy(() -> projectService.sendInvitationByEmail("owner@example.com", 1L, "member@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("프로젝트 소유자만");
    }

    @Test
    void sendInvitationByEmailCreatesPendingInvitationWhenOwner() {
        Member owner = new Member();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        Member invitee = new Member();
        invitee.setId(2L);
        invitee.setEmail("invitee@example.com");
        Project project = new Project();
        project.setTitle("Team Project");

        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(memberRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));
        when(projectMemberRepository.existsByProjectAndMember(project, invitee)).thenReturn(false);

        projectService.sendInvitationByEmail("owner@example.com", 1L, "invitee@example.com");

        ArgumentCaptor<ProjectInvitation> invitationCaptor = ArgumentCaptor.forClass(ProjectInvitation.class);
        verify(projectInvitationRepository).save(invitationCaptor.capture());
        assertThat(invitationCaptor.getValue().getProject()).isSameAs(project);
        assertThat(invitationCaptor.getValue().getSender()).isSameAs(owner);
        assertThat(invitationCaptor.getValue().getReceiver()).isSameAs(invitee);
        assertThat(invitationCaptor.getValue().getStatus()).isEqualTo(ProjectInvitationStatus.PENDING);
    }

    @Test
    void sendInvitationByEmailFailsWhenAlreadyMember() {
        Member owner = new Member();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        Member invitee = new Member();
        invitee.setId(2L);
        invitee.setEmail("invitee@example.com");
        Project project = new Project();
        project.setTitle("Team Project");

        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(memberRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));
        when(projectMemberRepository.existsByProjectAndMember(project, invitee)).thenReturn(true);

        assertThatThrownBy(() -> projectService.sendInvitationByEmail("owner@example.com", 1L, "invitee@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 프로젝트에 참여 중인 회원입니다.");
    }

    @Test
    void sendInvitationByEmailFailsWhenInviteeEmailDoesNotExist() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Project project = new Project();
        project.setTitle("Team Project");

        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));
        when(memberRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.sendInvitationByEmail("owner@example.com", 1L, "missing@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가입된 회원을 찾을 수 없습니다.");
    }

    @Test
    void sendInvitationByEmailFailsWhenInvitingSelf() {
        Member owner = new Member();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        Project project = new Project();
        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));

        assertThatThrownBy(() -> projectService.sendInvitationByEmail("owner@example.com", 1L, "owner@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자기 자신에게는 초대할 수 없습니다.");
    }

    @Test
    void sendInvitationByEmailFailsWhenPendingInvitationAlreadyExists() {
        Member owner = new Member();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        Member invitee = new Member();
        invitee.setId(2L);
        invitee.setEmail("invitee@example.com");
        Project project = new Project();
        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(memberRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));
        when(projectMemberRepository.existsByProjectAndMember(project, invitee)).thenReturn(false);
        when(projectInvitationRepository.existsByProjectAndReceiverAndStatus(project, invitee, ProjectInvitationStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> projectService.sendInvitationByEmail("owner@example.com", 1L, "invitee@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 초대를 보낸 회원입니다.");
    }

    @Test
    void updateProjectStatusChangesStatusWhenOwner() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Project project = new Project();
        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));

        projectService.updateProjectStatus("owner@example.com", 1L, ProjectStatus.COMPLETED);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void updateProjectStatusRequiresOwnerRole() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = new Project();
        ProjectMember membership = new ProjectMember(project, member, ProjectMemberRole.MEMBER);

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, member)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> projectService.updateProjectStatus("member@example.com", 1L, ProjectStatus.COMPLETED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("프로젝트 소유자만");
    }

    @Test
    void updateProjectStatusAllowsSubmittingCurrentStatus() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Project project = new Project();
        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));

        projectService.updateProjectStatus("owner@example.com", 1L, ProjectStatus.IN_PROGRESS);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void addProjectLinkFailsWhenProjectIsCompleted() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = completedProject();

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndMember(project, member)).thenReturn(true);

        assertThatThrownBy(() -> projectService.addProjectLink("member@example.com", 1L, "GitHub", "https://github.com/example/repo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void addProjectLinkFailsWhenProjectIsPaused() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = pausedProject();

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndMember(project, member)).thenReturn(true);

        assertThatThrownBy(() -> projectService.addProjectLink("member@example.com", 1L, "GitHub", "https://github.com/example/repo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void sendInvitationByEmailFailsWhenProjectIsCompleted() {
        Member owner = new Member();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        Project project = completedProject();
        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));

        assertThatThrownBy(() -> projectService.sendInvitationByEmail("owner@example.com", 1L, "invitee@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void sendInvitationFailsWhenProjectIsCompleted() {
        Member sender = new Member();
        sender.setEmail("sender@example.com");
        Project project = completedProject();
        ProjectMember senderMembership = new ProjectMember(project, sender, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, sender)).thenReturn(Optional.of(senderMembership));

        assertThatThrownBy(() -> projectService.sendInvitation("sender@example.com", 1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void addTaskFailsWhenProjectIsCompleted() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = completedProject();

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndMember(project, member)).thenReturn(true);

        assertThatThrownBy(() -> projectService.addTask("member@example.com", 1L, "Task", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void toggleTaskFailsWhenProjectIsCompleted() {
        Member member = new Member();
        member.setEmail("member@example.com");
        Project project = completedProject();

        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndMember(project, member)).thenReturn(true);

        assertThatThrownBy(() -> projectService.toggleTask("member@example.com", 1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void acceptInvitationFailsWhenProjectIsCompleted() {
        Member receiver = new Member();
        receiver.setId(1L);
        receiver.setEmail("receiver@example.com");
        Project project = completedProject();
        ProjectInvitation invitation = new ProjectInvitation(project, new Member(), receiver);

        when(memberRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(projectInvitationRepository.findById(1L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> projectService.acceptInvitation("receiver@example.com", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }

    @Test
    void deleteProjectAllowsCompletedProjectWhenOwner() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Project project = completedProject();
        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));

        projectService.deleteProject("owner@example.com", 1L);

        verify(projectRepository).delete(project);
    }

    private Project completedProject() {
        Project project = new Project();
        project.setStatus(ProjectStatus.COMPLETED);
        return project;
    }

    private Project pausedProject() {
        Project project = new Project();
        project.setStatus(ProjectStatus.PAUSED);
        return project;
    }
}
