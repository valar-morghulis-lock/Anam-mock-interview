package anam.interview.mock.controllers;

import anam.interview.mock.anam.*;
import anam.interview.mock.entities.InterviewSession;
import anam.interview.mock.entities.SessionQuestion;
import anam.interview.mock.exceptions.ResourceNotFoundException;
import anam.interview.mock.repositories.InterviewSessionRepository;
import anam.interview.mock.repositories.SessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/interviews/{sessionId}/session-token")
@RequiredArgsConstructor
public class SessionTokenController {

    private final AnamClient anamClient;
    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;

    private static final String AVATAR_ID = "6cc28442-cccd-42a8-b6e4-24b7210a09c5"; // Gabriel, cara-4
    private static final String VOICE_ID = "c674059e-302c-4bce-9bb8-7430802d5067";   // Tara - Confident Ally
    private static final String LLM_ID = "a7cf662c-2ace-4de1-a21e-ef0fbf144bb7";     // GPT OSS 120B

    @PostMapping
    public ResponseEntity<AnamSessionToken> createToken(@PathVariable UUID sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No such session: " + sessionId));

        List<SessionQuestion> questions = sessionQuestionRepository
                .findBySessionIdAndSkippedFalseOrderBySequenceNo(sessionId);

        PersonaConfig persona = personaFor(session, questions);
        AnamSessionToken token = anamClient.createSessionToken(persona, session.getTimeLimitSec());

        return ResponseEntity.ok(token);
    }

    private PersonaConfig personaFor(InterviewSession session, List<SessionQuestion> questions) {
        String questionList = IntStream.range(0, questions.size())
                .mapToObj(i -> (i + 1) + ". " + questions.get(i).getQuestion().getText())
                .collect(Collectors.joining("\n"));

        String toneInstruction = switch (session.getPersonaStyle()) {
            case SUPPORTIVE -> """
                You are warm and encouraging. You gently probe vague answers by asking for specifics
                (e.g. "What was the outcome?" or "How did the team react?"). Keep a supportive tone throughout.
                """;
            case BAR_RAISER -> """
                You are formal and exacting. You firmly probe vague or incomplete answers, pressing for
                concrete details, metrics, and outcomes. Maintain a professional, slightly demanding tone,
                but remain fair and never hostile.
                """;
        };

        String systemPrompt = """
            [STYLE] Reply in natural speech without formatting. Add pauses using '...' and occasionally a disfluency.
            [PERSONALITY] You are Gabriel, a behavioural interviewer for a %s (%s level) role.
            %s
            [QUESTIONS] Ask the following questions, one at a time, in this order. Do not skip ahead or
            combine questions. Wait for a complete answer before moving to the next question. Do not read
            the numbers aloud.
            %s
            [RULES] Never provide the answer yourself. Never tell the candidate what a good answer looks like.
            When all questions have been asked and answered, thank the candidate and let them know the
            interview is complete.
            """.formatted(session.getRole(), session.getSeniority(), toneInstruction, questionList);

        return new PersonaConfig("Gabriel", AVATAR_ID, VOICE_ID, LLM_ID, systemPrompt);
    }
}