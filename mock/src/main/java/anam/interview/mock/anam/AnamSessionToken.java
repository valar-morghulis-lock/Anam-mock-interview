package anam.interview.mock.anam;

public record AnamSessionToken(
        String sessionToken,
        int expiresInSec
) {}