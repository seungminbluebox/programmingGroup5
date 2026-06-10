package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectJoinRequestRepository extends JpaRepository<ProjectJoinRequest, Long> {

    List<ProjectJoinRequest> findAllByStatus(ProjectJoinRequestStatus status);

    List<ProjectJoinRequest> findAllByProjectAndApplicantAndStatus(
            Project project,
            Member applicant,
            ProjectJoinRequestStatus status
    );

    boolean existsByProjectAndApplicantAndStatus(
            Project project,
            Member applicant,
            ProjectJoinRequestStatus status
    );

    void deleteAllByProject(Project project);
}
