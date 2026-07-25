package anam.interview.mock.repositories;


import anam.interview.mock.entities.AnswerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerFeedbackRepository extends JpaRepository<AnswerFeedback, UUID> {

    Optional<AnswerFeedback> findBySessionQuestionId(UUID sessionQuestionId);

    // Join through session_question to get all feedback for a session in one query
    @Query("""
        SELECT af FROM AnswerFeedback af
        JOIN af.sessionQuestion sq
        WHERE sq.session.id = :sessionId
        ORDER BY sq.sequenceNo
        """)
    List<AnswerFeedback> findBySessionIdOrderBySequence(@Param("sessionId") UUID sessionId);
}