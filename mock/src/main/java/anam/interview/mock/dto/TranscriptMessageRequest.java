package anam.interview.mock.dto;

import anam.interview.mock.entities.TranscriptMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TranscriptMessageRequest(
        UUID sessionQuestionId,               // nullable — greeting has none
        @NotNull TranscriptMessage.Speaker speaker,
        @NotBlank String content
) {}