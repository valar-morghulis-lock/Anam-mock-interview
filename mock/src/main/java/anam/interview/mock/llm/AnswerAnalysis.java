package anam.interview.mock.llm;

public record AnswerAnalysis(
        boolean hasSituation,
        boolean hasTask,
        boolean hasAction,
        boolean hasResult,
        int score,
        String improvement
) {}