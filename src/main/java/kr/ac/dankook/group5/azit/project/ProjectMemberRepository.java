package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findAllByMember(Member member);

    boolean existsByProjectAndMember(Project project, Member member);

    Optional<ProjectMember> findByProjectAndMember(Project project, Member member);

    void deleteAllByProject(Project project);
}
