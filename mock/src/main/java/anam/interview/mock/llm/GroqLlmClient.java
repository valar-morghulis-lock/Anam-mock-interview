package anam.interview.mock.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Profile("!anthropic-llm")  // active by default — free-tier Groq
public class GroqLlmClient implements LlmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GroqLlmClient(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${groq.model:openai/gpt-oss-120b}") String model,
            ObjectMapper objectMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        var request = new ChatCompletionRequest(
                model,
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user", userPrompt)
                )
        );

        String rawResponse = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(String.class);

        try {
            JsonNode response = objectMapper.readTree(rawResponse);
            return response.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Groq response: " + rawResponse, e);
        }
    }

    private record ChatCompletionRequest(String model, List<Message> messages) {}
    private record Message(String role, String content) {}
}