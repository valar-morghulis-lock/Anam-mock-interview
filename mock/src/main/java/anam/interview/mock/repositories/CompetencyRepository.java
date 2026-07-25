package anam.interview.mock.repositories;


import anam.interview.mock.entities.Competency;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CompetencyRepository extends JpaRepository<Competency, UUID> {
    Optional<Competency> findByName(String name);
}