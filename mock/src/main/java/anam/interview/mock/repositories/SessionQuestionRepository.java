package anam.interview.mock.repositories;


import anam.interview.mock.entities.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, UUID> {

    List<SessionQuestion> findBySessionIdOrderBySequenceNo(UUID sessionId);

    List<SessionQuestion> findBySessionIdAndSkippedFalseOrderBySequenceNo(UUID sessionId);
}