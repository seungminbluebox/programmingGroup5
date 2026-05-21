package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    List<ProjectInvitation> findAllByReceiverAndStatus(Member receiver, ProjectInvitationStatus status);

    boolean existsByProjectAndReceiverAndStatus(
            Project project,
            Member receiver,
            ProjectInvitationStatus status
    );
}