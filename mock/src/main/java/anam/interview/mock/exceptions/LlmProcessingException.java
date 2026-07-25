package anam.interview.mock.exceptions;

public class LlmProcessingException extends RuntimeException {
    public LlmProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}