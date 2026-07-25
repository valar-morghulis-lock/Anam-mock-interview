package anam.interview.mock.anam;


public interface AnamClient {
    AnamSessionToken createSessionToken(PersonaConfig persona, int timeLimitSec);
}