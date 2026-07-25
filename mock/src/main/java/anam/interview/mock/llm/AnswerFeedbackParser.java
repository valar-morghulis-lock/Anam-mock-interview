package anam.interview.mock.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class AnswerFeedbackParser {

    private final ObjectMapper objectMapper; // injected, not instantiated

    public AnswerAnalysis parse(String rawLlmResponse) {
        try {
            String cleaned = rawLlmResponse.strip()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("```$", "");
            return objectMapper.readValue(cleaned, AnswerAnalysis.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse LLM response as AnswerAnalysis: " + rawLlmResponse, e);
        }
    }
}