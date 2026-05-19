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
    void addMemberToProjectRequiresOwnerRole() {
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

        assertThatThrownBy(() -> projectService.addMemberToProject("owner@example.com", 1L, "member@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("프로젝트 소유자만");
    }

    @Test
    void addMemberToProjectAddsNewMemberWhenOwner() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Member invitee = new Member();
        invitee.setEmail("invitee@example.com");
        Project project = new Project();
        project.setTitle("Team Project");

        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(memberRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));
        when(projectMemberRepository.existsByProjectAndMember(project, invitee)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectMember addedMember = projectService.addMemberToProject("owner@example.com", 1L, "invitee@example.com");

        assertThat(addedMember.getProject()).isSameAs(project);
        assertThat(addedMember.getMember()).isSameAs(invitee);
        assertThat(addedMember.getRole()).isEqualTo(ProjectMemberRole.MEMBER);
    }

    @Test
    void addMemberToProjectFailsWhenAlreadyMember() {
        Member owner = new Member();
        owner.setEmail("owner@example.com");
        Member invitee = new Member();
        invitee.setEmail("invitee@example.com");
        Project project = new Project();
        project.setTitle("Team Project");

        ProjectMember ownerMembership = new ProjectMember(project, owner, ProjectMemberRole.OWNER);

        when(memberRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(memberRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndMember(project, owner)).thenReturn(Optional.of(ownerMembership));
        when(projectMemberRepository.existsByProjectAndMember(project, invitee)).thenReturn(true);

        assertThatThrownBy(() -> projectService.addMemberToProject("owner@example.com", 1L, "invitee@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 프로젝트 멤버입니다.");
    }
}
