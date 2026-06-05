package kr.ac.dankook.group5.azit.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByStatus(ProjectStatus status);
}
