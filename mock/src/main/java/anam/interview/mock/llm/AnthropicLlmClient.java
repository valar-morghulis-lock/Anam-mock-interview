package anam.interview.mock.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AnthropicLlmClient implements LlmClient {

    private final RestClient restClient;
    private final String model;

    public AnthropicLlmClient(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.base-url:https://api.anthropic.com}") String baseUrl,
            @Value("${llm.model:claude-sonnet-4-6}") String model) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        var request = new CompletionRequest(
                model, 1000, systemPrompt,
                List.of(new Message("user", userPrompt))
        );

        JsonNode response = restClient.post()
                .uri("/v1/messages")
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        return response.path("content").get(0).path("text").asText();
    }

    private record CompletionRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            String system,
            List<Message> messages
    ) {}

    private record Message(String role, String content) {}
}