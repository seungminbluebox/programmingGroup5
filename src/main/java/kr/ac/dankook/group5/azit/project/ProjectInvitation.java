package kr.ac.dankook.group5.azit.project;

import jakarta.persistence.*;
import kr.ac.dankook.group5.azit.user.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProjectInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member receiver;

    @Enumerated(EnumType.STRING)
    private ProjectInvitationStatus status;

    private LocalDateTime createdAt;

    public ProjectInvitation(Project project, Member sender, Member receiver) {
        this.project = project;
        this.sender = sender;
        this.receiver = receiver;
        this.status = ProjectInvitationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = ProjectInvitationStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ProjectInvitationStatus.REJECTED;
    }
}