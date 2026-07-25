package anam.interview.mock.anam;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("mock-anam")
public class MockAnamClient implements AnamClient {

    @Override
    public AnamSessionToken createSessionToken(PersonaConfig persona, int timeLimitSec) {
        // No real Anam call — returns a fake token so the rest of the
        // pipeline (endpoint wiring, frontend fetch, time-limit config) is testable.
        String fakeToken = "mock-session-token-" + UUID.randomUUID();
        return new AnamSessionToken(fakeToken, timeLimitSec);
    }
}