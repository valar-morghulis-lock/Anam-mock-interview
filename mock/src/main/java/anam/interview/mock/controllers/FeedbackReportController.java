package anam.interview.mock.controllers;

import anam.interview.mock.dto.FeedbackReportResponse;
import anam.interview.mock.service.FeedbackGenerationService;
import anam.interview.mock.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interviews/{sessionId}/report")
@RequiredArgsConstructor
public class FeedbackReportController {

    private final FeedbackGenerationService feedbackGenerationService;
    private final PdfReportService pdfReportService;

    @PostMapping
    public ResponseEntity<FeedbackReportResponse> generate(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(feedbackGenerationService.generateReport(sessionId));
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID sessionId) {
        byte[] pdf = pdfReportService.generatePdf(sessionId);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"interview-report.pdf\"").body(pdf);
    }
}