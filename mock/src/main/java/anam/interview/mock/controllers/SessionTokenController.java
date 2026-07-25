package anam.interview.mock.controllers;

import anam.interview.mock.anam.*;
import anam.interview.mock.entities.InterviewSession;
import anam.interview.mock.exceptions.ResourceNotFoundException;
import anam.interview.mock.repositories.InterviewSessionRepository;
import anam.interview.mock.service.PersonaBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interviews/{sessionId}/session-token")
@RequiredArgsConstructor
public class SessionTokenController {

    private final AnamClient anamClient;
    private final InterviewSessionRepository sessionRepository;
    private final PersonaBuilderService personaBuilderService;

    @PostMapping
    public ResponseEntity<AnamSessionToken> createToken(@PathVariable UUID sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No such session: " + sessionId));

        PersonaConfig persona = personaBuilderService.buildPersona(sessionId);
        AnamSessionToken token = anamClient.createSessionToken(persona, session.getTimeLimitSec());

        return ResponseEntity.ok(token);
    }
}