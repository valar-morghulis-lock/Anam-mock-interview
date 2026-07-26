package anam.interview.mock.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedbackReportResponse(
        UUID reportId,
        UUID sessionId,
        String overallStrengths,
        Instant createdAt,
        List<AnswerSummary> answers
) {
    public record AnswerSummary(
            String questionText,
            boolean hasSituation,
            boolean hasTask,
            boolean hasAction,
            boolean hasResult,
            int score,
            String improvement
    ) {}
}