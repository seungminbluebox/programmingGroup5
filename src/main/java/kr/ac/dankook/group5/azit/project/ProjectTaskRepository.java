package kr.ac.dankook.group5.azit.project;

import kr.ac.dankook.group5.azit.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findAllByProjectOrderByIdDesc(Project project);

    List<ProjectTask> findAllByProjectAndAssignee(Project project, Member assignee);
}