package kr.ac.dankook.group5.azit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    Optional<TechStack> findByStackName(String stackName);
}
