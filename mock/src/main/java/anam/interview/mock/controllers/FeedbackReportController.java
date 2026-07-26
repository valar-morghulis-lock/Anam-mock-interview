package anam.interview.mock.controllers;

import anam.interview.mock.dto.FeedbackReportResponse;
import anam.interview.mock.entities.FeedbackReport;
import anam.interview.mock.service.FeedbackGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interviews/{sessionId}/report")
@RequiredArgsConstructor
public class FeedbackReportController {

    private final FeedbackGenerationService feedbackGenerationService;

    @PostMapping
    public ResponseEntity<FeedbackReportResponse> generate(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(feedbackGenerationService.generateReport(sessionId));
    }
}