package anam.interview.mock.repositories;


import anam.interview.mock.entities.FeedbackReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, UUID> {
    Optional<FeedbackReport> findBySessionId(UUID sessionId);
}