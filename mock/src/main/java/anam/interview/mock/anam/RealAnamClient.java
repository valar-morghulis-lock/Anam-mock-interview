package anam.interview.mock.anam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!mock-anam")
public class RealAnamClient implements AnamClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RealAnamClient(@Value("${anam.api-key}") String apiKey,
                          @Value("${anam.base-url:https://api.anam.ai}") String baseUrl,
                          ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public AnamSessionToken createSessionToken(PersonaConfig persona, int timeLimitSec) {
        String rawResponse = restClient.post()
                .uri("/v1/auth/session-token")
                .body(new SessionTokenRequest(persona))
                .retrieve()
                .body(String.class);

        JsonNode response;
        try {
            response = objectMapper.readTree(rawResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Anam session-token response: " + rawResponse, e);
        }

        return new AnamSessionToken(
                response.path("sessionToken").asText(),
                timeLimitSec
        );
    }

    private record SessionTokenRequest(PersonaConfig personaConfig) {}
}