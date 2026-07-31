package anam.interview.mock.service;

import anam.interview.mock.entities.Competency;
import anam.interview.mock.entities.Question;
import anam.interview.mock.entities.Question.QuestionSource;
import anam.interview.mock.llm.LlmClient;
import anam.interview.mock.repositories.CompetencyRepository;
import anam.interview.mock.repositories.QuestionRepository;
import anam.interview.mock.util.TagNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionGenerationService {

    private static final int MIN_POOL_SIZE = 5;
    private static final int GENERATE_BATCH_SIZE = 8;

    private final QuestionRepository questionRepository;
    private final CompetencyRepository competencyRepository;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public void ensurePoolIsStocked(String competencyName, String roleTag, String languageTag) {
        roleTag = TagNormalizer.normalize(roleTag);
        languageTag = TagNormalizer.normalize(languageTag);

        Competency competency = competencyRepository.findByName(competencyName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown competency: " + competencyName));

        long existing = questionRepository.countByCompetencyAndRoleAndLanguage(
                competency.getId(), roleTag, languageTag);

        if (existing >= MIN_POOL_SIZE) {
            return;
        }

        List<String> generated = generateQuestions(competencyName, roleTag, languageTag);

        for (String text : generated) {
            boolean isDuplicate = questionRepository.findSimilar(competency.getId(), text).isPresent();
            if (isDuplicate) continue;

            Question question = Question.builder()
                    .text(text)
                    .competency(competency)
                    .roleTag(roleTag)
                    .languageTag(languageTag)
                    .source(QuestionSource.GENERATED)
                    .build();
            questionRepository.save(question);
        }
    }

    private List<String> generateQuestions(String competencyName, String roleTag, String languageTag) {
        String languageContext = languageTag != null ? " using " + languageTag : "";
        String systemPrompt = """
            You generate interview questions for a mock interview platform. Respond with ONLY a JSON
            array of strings, no markdown fences, no preamble. Each string is one complete interview
            question. Questions must be specific, unambiguous, and answerable in 1-3 minutes of speech.
            """;

        String userPrompt = """
            Generate %d interview questions testing "%s" for a "%s" role%s.
            Return as a JSON array of strings only.
            """.formatted(GENERATE_BATCH_SIZE, competencyName, roleTag, languageContext);

        String rawResponse = llmClient.complete(systemPrompt, userPrompt);

        try {
            String cleaned = rawResponse.strip()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("```$", "");
            return objectMapper.readValue(cleaned, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse generated questions: " + rawResponse, e);
        }
    }
}