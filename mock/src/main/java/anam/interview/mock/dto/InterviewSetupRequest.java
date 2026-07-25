package anam.interview.mock.dto;

import anam.interview.mock.entities.InterviewSession;
import jakarta.validation.constraints.*;
import java.util.List;

public record InterviewSetupRequest(
        @NotBlank String role,
        @NotNull InterviewSession.Seniority seniority,
        @NotNull InterviewSession.PersonaStyle personaStyle,
        @NotEmpty List<String> competencyNames,   // for instance ["conflict", "failure"]
        @Min(1) @Max(10) int questionsPerCompetency,
        @Min(60) @Max(180) int timeLimitSec
) {}