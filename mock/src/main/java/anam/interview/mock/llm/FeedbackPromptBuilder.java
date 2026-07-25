package anam.interview.mock.llm;

public class FeedbackPromptBuilder {

    private static final String SYSTEM_PROMPT = """
        You are an expert behavioural interview coach evaluating a candidate's answer
        using the STAR method (Situation, Task, Action, Result).

        Respond with ONLY a JSON object, no markdown fences, no preamble, matching exactly:
        {"hasSituation": boolean, "hasTask": boolean, "hasAction": boolean,
         "hasResult": boolean, "score": integer 1-5, "improvement": "one sentence"}

        Scoring guide: 5 = all four STAR elements present with specifics;
        3 = situation and action present but task or result vague/missing;
        1 = little more than a situation description, no clear action or outcome.
        """;

    public static String system() {
        return SYSTEM_PROMPT;
    }

    public static String userPrompt(String question, String candidateAnswer) {
        return """
            Interview question: %s

            Candidate's answer: %s

            Evaluate this answer.
            """.formatted(question, candidateAnswer);
    }
}