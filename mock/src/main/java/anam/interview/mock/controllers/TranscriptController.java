package anam.interview.mock.controllers;

import anam.interview.mock.entities.TranscriptMessage;
import anam.interview.mock.dto.TranscriptMessageRequest;
import anam.interview.mock.service.TranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews/{sessionId}/transcript")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    @PostMapping
    public ResponseEntity<Void> append(@PathVariable UUID sessionId,
                                       @Valid @RequestBody TranscriptMessageRequest request) {
        transcriptService.appendMessage(sessionId, request.sessionQuestionId(),
                request.speaker(), request.content());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/questions/{sessionQuestionId}/skip")
    public ResponseEntity<Void> skip(@PathVariable UUID sessionId, @PathVariable UUID sessionQuestionId) {
        transcriptService.markSkipped(sessionQuestionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TranscriptMessage>> get(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(transcriptService.getTranscript(sessionId));
    }
}