package kr.ac.dankook.group5.azit.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectAvailabilityRepository extends JpaRepository<ProjectAvailability, Long> {
    List<ProjectAvailability> findAllByProject(Project project);
    void deleteAllByProject(Project project);
}
