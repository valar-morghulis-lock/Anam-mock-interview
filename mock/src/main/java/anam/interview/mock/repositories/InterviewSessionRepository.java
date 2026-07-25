package anam.interview.mock.repositories;


import anam.interview.mock.entities.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {

    List<InterviewSession> findByStatusOrderByCreatedAtDesc(InterviewSession.SessionStatus status);

    List<InterviewSession> findAllByOrderByCreatedAtDesc(); // for "scores across sessions" (Step 6)
}