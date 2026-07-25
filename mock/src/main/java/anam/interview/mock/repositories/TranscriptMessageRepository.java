package anam.interview.mock.repositories;


import anam.interview.mock.entities.TranscriptMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TranscriptMessageRepository extends JpaRepository<TranscriptMessage, UUID> {

    List<TranscriptMessage> findBySessionIdOrderBySpokenAt(UUID sessionId);

    List<TranscriptMessage> findBySessionQuestionIdOrderBySpokenAt(UUID sessionQuestionId);
}