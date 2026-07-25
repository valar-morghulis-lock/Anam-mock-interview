package anam.interview.mock.controllers;

import anam.interview.mock.dto.InterviewSessionResponse;
import anam.interview.mock.dto.InterviewSetupRequest;
import anam.interview.mock.service.InterviewSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewSetupController {

    private final InterviewSetupService interviewSetupService;

    @PostMapping
    public ResponseEntity<InterviewSessionResponse> create(@Valid @RequestBody InterviewSetupRequest request) {
        return ResponseEntity.ok(interviewSetupService.createSession(request));
    }
}