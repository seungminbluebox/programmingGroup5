package kr.ac.dankook.group5.azit.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectStackRepository extends JpaRepository<ProjectStack, Long> {
    List<ProjectStack> findAllByProject(Project project);
    List<ProjectStack> findAllByProjectAndRequired(Project project, boolean required);
    void deleteAllByProject(Project project);
}
