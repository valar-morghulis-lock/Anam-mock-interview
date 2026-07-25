package anam.interview.mock.anam;

public record PersonaConfig(
        String name,
        String avatarId,
        String voiceId,
        String llmId,
        String systemPrompt
) {}