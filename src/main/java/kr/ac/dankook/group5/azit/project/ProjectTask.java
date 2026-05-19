package kr.ac.dankook.group5.azit.project;

import jakarta.persistence.*;
import kr.ac.dankook.group5.azit.user.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member assignee;

    public ProjectTask(Project project, Member assignee, String title) {
        this.project = project;
        this.assignee = assignee;
        this.title = title;
        this.completed = false;
    }

    public void toggleCompleted() {
        this.completed = !this.completed;
    }
}