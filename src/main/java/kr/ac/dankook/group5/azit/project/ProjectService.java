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

    @Transactional
    public Project createProject(String ownerEmail, String title, String description) {
        Member owner = getMemberByEmail(ownerEmail);
        Project project = projectRepository.save(new Project(required(title, "프로젝트 이름"), required(description, "프로젝트 설명")));
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

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "을 입력해 주세요.");
        }
        return value.trim();
    }
}
