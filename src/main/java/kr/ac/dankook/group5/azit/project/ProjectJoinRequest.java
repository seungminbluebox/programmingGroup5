package kr.ac.dankook.group5.azit.project;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import kr.ac.dankook.group5.azit.user.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProjectJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member applicant;

    @Enumerated(EnumType.STRING)
    private ProjectJoinRequestStatus status;

    private LocalDateTime createdAt;

    public ProjectJoinRequest(Project project, Member applicant) {
        this.project = project;
        this.applicant = applicant;
        this.status = ProjectJoinRequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = ProjectJoinRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ProjectJoinRequestStatus.REJECTED;
    }
}
