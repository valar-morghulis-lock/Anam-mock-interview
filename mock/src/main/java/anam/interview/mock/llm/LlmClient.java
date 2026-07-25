package anam.interview.mock.llm;

public interface LlmClient {
    String complete(String systemPrompt, String userPrompt);
}