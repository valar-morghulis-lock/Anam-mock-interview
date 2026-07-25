package anam.interview.mock.dto;

import java.util.List;
import java.util.UUID;

public record InterviewSessionResponse(
        UUID sessionId,
        String role,
        String seniority,
        String personaStyle,
        List<QuestionSummary> questions
) {
    public record QuestionSummary(UUID sessionQuestionId, String text, int sequenceNo) {}
}